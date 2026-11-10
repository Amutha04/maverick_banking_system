import axios from "axios";
import { useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux"
import { API_SERVER_URL_DEV } from "../../config";
import { getLoans } from "../../store/actions/CustomerActions";

function LoanProduct() {
    const loanProduct = useSelector(state => state.loanProducts)
    const accountList = useSelector(state => state.customerAccounts)
    const dispatch = useDispatch()

    const [fromAccount, setFromAccount] = useState("")
    const [loanType, setLoanType] = useState("")
    const [amount, setAmount] = useState(0)
    const [months, setMonths] = useState(0)
    const [purpose, setPurpose] = useState(0)

    const openReqRef = useRef(null)

     const formatAmount = (amount) => {
        if (amount === null || amount === undefined) return "As per asset value";
        const n = Number(amount);
        if (n >= 10000000) return `₹${(n / 10000000)} crore`;
        if (n >= 100000) return `₹${(n / 100000)} lakh`;
        return `₹${n.toLocaleString("en-IN")}`;
    }

     const formatTenure = (months) => {
        const y = Math.floor(months / 12);
        const m = months % 12;
        const parts = [];
        if (y) parts.push(`${y} ${y === 1 ? "year" : "years"}`);
        if (m) parts.push(`${m} ${m === 1 ? "month" : "months"}`);
        return parts.join(" ") || "0 months";
    }

    const applyLoan = async(e) => {
        e.preventDefault()

        let obj = {
            loanType : loanType,
            amount : amount,
            tenureMonths : months,
            purpose : purpose
        }

        let acc = accountList.find(ac => ac.accountNumber == fromAccount)
        if(!acc) {
            window.Swal.fire({
                icon: "warning",
                title: "Select an account",
                text: "Choose the account that should receive the loan",
                confirmButtonText: "OK"
            })
            return
        }
        let accountId = acc.id

        try{
            await axios.post(`${API_SERVER_URL_DEV}/api/loan/request/${accountId}`, obj, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })

            openReqRef.current.click()

            window.Swal.fire({
                icon: "success",
                title: "Your request has been sent",
                showConfirmButton: false,
                timer: 1500
            })

            dispatch(getLoans())
        }
        catch (err) {
            openReqRef.current.click()
            window.Swal.fire({
                icon: "error",
                title: "Request failed",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    return (
        <div className="card mb-4">
            <div className="card-header bg-white fw-semibold">Available loans</div>
            <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Loan</th>
                            <th>Amount</th>
                            <th>Interest rate</th>
                            <th>Tenure</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            loanProduct.map((l, index) => (

                                <tr key={index}>
                                    <td>#{l.id}</td>
                                    <td>{l.loanType}</td>
                                    <td>Up to {formatAmount(l.maxAmount)}</td>
                                    <td>Starting from {l.interestRate}%</td>
                                    <td> Up to {formatTenure(l.tenureMonths)}</td>
                                    <td><button className="btn btn-maverick" data-bs-toggle="modal" data-bs-target="#loanModal" onClick={()=>{setLoanType(l.loanType)
                                        setFromAccount("")
                                        setAmount("")
                                        setPurpose("")
                                        setMonths("")
                                    }}>Apply</button></td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
            </div>

            <div className="modal fade" id="loanModal" tabIndex="-1"><div className="modal-dialog">
                <form className="modal-content needs-validation" onSubmit={(e)=>applyLoan(e)} data-success="Loan application submitted. A bank employee will review it.">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Apply for loan</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div className="modal-body row g-3">
                            <div className="col-12">
                                <label className="form-label">Account</label>
                                <select className="form-select" value={fromAccount} onChange={(e)=>setFromAccount(e.target.value)} required>
                                <option value="">Select account</option>
                                {
                                    accountList.map((ac,index) => (
                                        ac.status === 'ACTIVE' &&
                                        <option key={index} value={ac.accountNumber}>{ac.accountType} - {ac.accountNumber}</option>
                                    ))
                                }
                            </select>
                            </div>
                            <div className="col-12">
                                <label className="form-label">Loan type</label>
                                <input id="loan-type" className="form-control" value = {loanType} readOnly />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Amount (₹)</label>
                                    <input type="number" className="form-control" value={amount} onChange={(e)=>setAmount(e.target.value)}  placeholder="" required min = "10000"/></div>
                                    <div className="col-md-6">
                                        <label className="form-label">Tenure (months)</label>
                                        <input type="number" className="form-control" value={months} onChange={(e)=>setMonths(e.target.value)} placeholder="" required min="6" /></div>
                                        <div className="col-12">
                                            <label className="form-label">Purpose</label>
                                            <textarea className="form-control" value={purpose} onChange={(e)=>setPurpose(e.target.value)} rows="2" required></textarea>
                                            </div></div>
                        <div className="modal-footer"><button type="button" className="btn btn-light" data-bs-dismiss="modal" ref={openReqRef}>Cancel</button>
                        <button className="btn btn-primary">Submit application</button></div></form></div></div>
        </div>
    )
}

export default LoanProduct