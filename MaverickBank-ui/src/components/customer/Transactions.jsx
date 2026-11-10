import { useDispatch, useSelector } from "react-redux";
import { useState } from "react";
import { Link } from "react-router";
import axios from "axios";
import { API_SERVER_URL_DEV } from "../../config";
import CheckBalance from "./CheckBalance";
import { getAccounts, getTransactions } from "../../store/actions/CustomerActions";

function Transactions() {

    const accountList = useSelector(state => state.customerAccounts)
    const beneficiaryList = useSelector(state => state.beneficiary)
    const memToken = localStorage.getItem('token')
    const dispatch = useDispatch()

    const [fromAccount, setFromAccount] = useState(0)
    const [toAccount, setToAccount] = useState(0)
    const [amount, setAmount] = useState(0)
    const [remark, setRemark] = useState("")
    const [password, setPassword] = useState("")
    const [otherAccount, setOtherAccount] = useState("")

    const [showWdBalance, setShowWdBalance] = useState(false)
    const [showTrBalance, setShowTrBalance] = useState(false)

    const onDeposit = async (e) => {
        e.preventDefault()

        try {
            let obj = {
                amount: amount,
                remarks: remark
            }

            let acc = accountList.find(ac => ac.accountNumber == fromAccount)
            let accountId = acc.id
            await axios.put(`${API_SERVER_URL_DEV}/api/account/deposit/${accountId}`, obj, {
                headers: { Authorization: 'Bearer ' + memToken }
            })

            window.Swal.fire({
                icon: "success",
                title: "Transaction Success",
                showConfirmButton: false,
                timer: 1500
            })

            // reload the balances and the transaction history
            dispatch(getAccounts())
            dispatch(getTransactions())
        }
        catch (err) {

            window.Swal.fire({
                icon: "error",
                title: "Transaction Failed",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }


    }

    const onWithdraw = async (e) => {
        e.preventDefault()

        try {
            let obj = {
                amount: amount,
                password: password,
                remarks: remark
            }

            let acc = accountList.find(ac => ac.accountNumber == fromAccount)
            let accountId = acc.id
            await axios.put(`${API_SERVER_URL_DEV}/api/account/withdraw/${accountId}`, obj, {
                headers: { Authorization: 'Bearer ' + memToken }
            })

            window.Swal.fire({
                icon: "success",
                title: "Transaction Success",
                showConfirmButton: false,
                timer: 1500
            })

           
            dispatch(getAccounts())
            dispatch(getTransactions())
        }
        catch (err) {

            window.Swal.fire({
                icon: "error",
                title: "Transaction Failed",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }

    }
    const onTransaction = async (e) => {
        e.preventDefault()

        try {
            let obj = {
                amount: amount,
                destinationAccount: toAccount === "Other" ? otherAccount : toAccount,
                password: password,
                remarks: remark
            }

            let acc = accountList.find(ac => ac.accountNumber == fromAccount)
            let accountId = acc.id
            await axios.post(`${API_SERVER_URL_DEV}/api/transaction/add/${accountId}`, obj, {
                headers: { Authorization: 'Bearer ' + memToken }
            })

            window.Swal.fire({
                icon: "success",
                title: "Transaction Success",
                showConfirmButton: false,
                timer: 1500
            })

            
            dispatch(getAccounts())
            dispatch(getTransactions())
        }
        catch (err) {

            window.Swal.fire({
                icon: "error",
                title: "Transaction Failed",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }
    return (

        <div className="card mx-auto" style={{ maxWidth: "1000px" }}><div className="card-body">
            <ul className="nav nav-pills mb-4"><li className="nav-item"><button className="nav-link active" data-bs-toggle="pill" data-bs-target="#dep">Deposit</button></li>
                <li className="nav-item">
                    <button className="nav-link" data-bs-toggle="pill" data-bs-target="#wd">Withdraw</button>
                </li>
                <li className="nav-item">
                    <button className="nav-link" data-bs-toggle="pill" data-bs-target="#tr">Transfer</button>
                </li>
            </ul>

            <div className="tab-content">
                <div className="tab-pane fade show active" id="dep">
                    <form className="row g-3 needs-validation" data-success="Deposit successful." onSubmit={(e) => onDeposit(e)}>
                        <div className="col-md-6">
                            <label className="form-label">Account</label>
                            <select className="form-select" onChange={(e) => setFromAccount(e.target.value)} required>
                                <option value="">Select account</option>
                                {
                                    accountList.map((ac, index) => (
                                        ac.status === 'ACTIVE' &&
                                        <option key={index} value={ac.accountNumber}>{ac.accountType} - {ac.accountNumber}</option>
                                    ))
                                }
                            </select>
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Amount (₹)</label>
                            <input type="number" onChange={(e) => setAmount(e.target.value)} className="form-control" placeholder="" required min="1" step="0.01" />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Remarks</label>
                            <input className="form-control" onChange={(e) => setRemark(e.target.value)} placeholder="Optional" />
                        </div>
                        <div className="col-12">
                            <button className="btn btn-maverick">Deposit</button>
                        </div>
                    </form>
                </div>


                <div className="tab-pane fade" id="wd">
                    <form className="row g-3 needs-validation" onSubmit={(e) => onWithdraw(e)} data-success="Withdrawal successful.">
                        <div className="col-md-6">
                            <label className="form-label">Account</label>
                            <select className="form-select" onChange={(e) => setFromAccount(e.target.value)} required>
                                <option value="">Select account</option>
                                {
                                    accountList.map((ac, index) => (
                                        (ac.status === 'ACTIVE' || ac.status === 'CLOSURE_REQUEST') &&
                                        <option key={index} value={ac.accountNumber}>{ac.accountType} - {ac.accountNumber}</option>
                                    ))
                                }
                            </select>
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Amount (₹)</label>
                            <input type="number" onChange={(e) => setAmount(e.target.value)} className="form-control" placeholder="" required min="1" step="0.01" />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Password</label>
                            <input type="password" className="form-control" onChange={(e) => setPassword(e.target.value)} placeholder="" />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Remarks</label>
                            <input className="form-control" onChange={(e) => setRemark(e.target.value)} placeholder="Optional" />
                        </div>
                        <div className="col-12">
                            <button className="btn btn-maverick">Withdraw</button>
                            <button type="button" className="btn btn-outline-secondary ms-2"
                                onClick={() => setShowWdBalance(!showWdBalance)}>
                                Check balance
                            </button>
                        </div>
                    </form>
                    {showWdBalance && <CheckBalance accountList={accountList} />}
                </div>


                <div className="tab-pane fade" id="tr">
                    <form className="row g-3 needs-validation" onSubmit={(e) => onTransaction(e)} data-success="Transfer successful.">
                        <div className="col-md-6">
                            <label className="form-label">From account</label>
                            <select className="form-select" onChange={(e) => setFromAccount(e.target.value)} required>
                                <option value="">Select account</option>
                                {
                                    accountList.map((ac, index) => (
                                        (ac.status === 'ACTIVE' || ac.status === 'CLOSURE_REQUEST') &&
                                        <option key={index} value={ac.accountNumber}>{ac.accountType} - {ac.accountNumber}</option>
                                    ))
                                }
                            </select>
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">To Account</label>
                            <select onChange={(e) => setToAccount(e.target.value)} className="form-select" required>
                                <option value="">Select beneficiary</option>
                                {
                                    beneficiaryList.map((bc, index) => (
                                        <option key={index} value={bc.accountNumber}>{bc.name} - {bc.accountNumber}</option>
                                    ))
                                }
                                <option value="Other">Other</option>
                            </select>
                            <div className="form-text">Not listed? <Link to={`/customer/${memToken}/beneficiaries`}>Add a beneficiary</Link></div>

                        </div>

                        {(toAccount === "Other" && <div className="col-md-6">
                            <label className="form-label">Enter Account Number</label>
                            <input type="number" className="form-control" placeholder="" required inputMode="numeric" value={otherAccount} onChange={(e) => setOtherAccount(e.target.value)} />
                        </div>)}

                        <div className="col-md-6">
                            <label className="form-label">Amount (₹)</label>
                            <input type="number" onChange={(e) => setAmount(e.target.value)} className="form-control" placeholder="" required min="1" step="0.01" />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Password</label>
                            <input type="password" className="form-control" onChange={(e) => setPassword(e.target.value)} placeholder="" />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label">Remarks</label>
                            <input className="form-control" onChange={(e) => setRemark(e.target.value)} placeholder="Optional" />
                        </div>
                        <div className="col-12">
                            <button className="btn btn-maverick">Transfer</button>
                            <button type="button" className="btn btn-outline-secondary ms-2"
                                onClick={() => setShowTrBalance(!showTrBalance)}>
                                Check balance
                            </button>
                        </div>
                    </form>
                    {showTrBalance && <CheckBalance accountList={accountList}/>}
               
            </div>
        </div>
        </div>
        </div >

    )
}

export default Transactions