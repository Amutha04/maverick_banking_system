import axios from "axios"
import { useRef, useState } from "react"
import { useDispatch, useSelector } from "react-redux"
import { API_SERVER_URL_DEV } from "../../config"
import { getAccounts } from "../../store/actions/CustomerActions"
import StatusBadge from "../common/StatusBadge"

function AccountList({ onRequested }) {

    const accountList = useSelector(state => state.customerAccounts)
    const branchList = useSelector(state => state.branches)
    const dispatch = useDispatch()

    const [viewAcc, setViewAcc] = useState(null)
    const [txnPassword, setTxnPassword] = useState("")

    const [newAccountType, setNewAccountType] = useState("")
    const [aadhaarNumber, setAadhaarNumber] = useState("")
    const [panNumber, setPanNumber] = useState("")
    const [branch, setBranch] = useState("")
    const [aadhaarFile, setAadhaarFile] = useState(null)
    const [photoFile, setPhotoFile] = useState(null)
    const [signatureFile, setSignatureFile] = useState(null)
    const [panFile, setPanFile] = useState(null)
    const [initialDeposit, setInitialDeposit] = useState("")

    const selectedBranch = branchList.find(br => br.name === branch)

    const [accountNumber, setAccountNumber] = useState(0)
    const [accountId, setAccountId] = useState(0)
    const [reason, setReason] = useState("")
    const [accountType, setAccountType] = useState(null)
    const [error, setError] = useState("");

    const [statusFilter, setStatusFilter] = useState("ALL")
    const filteredAccounts = statusFilter === "ALL" ? accountList : accountList.filter(ac => ac.status === statusFilter)

    const closeReqRef = useRef(null)
    const openReqRef = useRef(null)
    const [agreed, setAgreed] = useState(false)

    const onSetPassword = async (e) => {
        e.preventDefault()
        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/account/set-password/${viewAcc.id}`, { password: txnPassword }, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            setTxnPassword("")
            window.Swal.fire({
                icon: "success",
                title: "Transaction password set",
                showConfirmButton: false,
                timer: 1500
            })
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not set the password",
                text: err?.response?.data?.message || err?.response?.data?.password || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onView = (ac) => {
        let obj = {
            id: ac.id,
            name: ac.name,
            accountNumber: ac.accountNumber,
            accountType: ac.accountType,
            status: ac.status,
            balance: ac.balance,
            openedOn: ac.openedOn,
            branchName: ac.branchName,
            district: ac.district,
            ifscCode: ac.ifscCode,
            executiveName: ac.executiveName,
            executiveEmail: ac.executiveEmail
        }

        setViewAcc(obj)
    }

    const onCloseAccont = async (e) => {
        e.preventDefault()

        if (!agreed) {
      setError("You must check the box to confirm you understand the account will be closed.");
      return;
    }
    setError("");

        let obj = {
            requestType: "CLOSE",
            accountType: accountType,
            reason : reason
        }

        try {
            await axios.post(`${API_SERVER_URL_DEV}/api/account/request?accountId=${accountId}`, obj, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })

            closeReqRef.current.click()

            window.Swal.fire({
                icon: "success",
                title: "Your request has been sent",
                showConfirmButton: false,
                timer: 1500
            })


            dispatch(getAccounts())
            onRequested?.()
        }
        catch (err) {
            closeReqRef.current.click()
            window.Swal.fire({
                icon: "error",
                title: "Request failed",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }

    }

    const onOpenAccount = async (e) => {
        e.preventDefault()
        let obj = {
            requestType: "OPEN",
            accountType: newAccountType,
            aadhaarNumber: aadhaarNumber,
            panNumber: panNumber,
            branchName: branch,
            initialDeposit: initialDeposit
        }

        const formData = new FormData()
        formData.append("aadhaar", aadhaarFile)
        formData.append("pan", panFile)
        formData.append("photo", photoFile)
        formData.append("signature", signatureFile)

        try {
           
            await axios.put(API_SERVER_URL_DEV + "/api/customer/kyc-upload", formData,
                {
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
                }
            )

            await axios.post(`${API_SERVER_URL_DEV}/api/account/request`, obj, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })

            openReqRef.current.click()

            window.Swal.fire({
                icon: "success",
                title: "Your request has been sent",
                showConfirmButton: false,
                timer: 1500
            })

            onRequested?.()
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
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <select className="form-select form-select-sm w-auto fw-semibold border-0 ps-0" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                    <option value="ALL">All accounts</option>
                    <option value="ACTIVE">Active accounts</option>
                    <option value="CLOSURE_REQUEST">Closure requested accounts</option>
                    <option value="SUSPENDED">Suspended accounts</option>
                    <option value="CLOSED">Closed accounts</option>
                </select>
                <div ><button className="btn btn-maverick btn-sm" data-bs-toggle="modal" data-bs-target="#openAccountModal" onClick={() => {
                    setNewAccountType("")
                    setAadhaarNumber("")
                    setAadhaarFile(null)
                    setPhotoFile(null)
                    setSignatureFile(null)
                    setPanNumber("")
                    setBranch("")
                    setPanFile(null)
                    setInitialDeposit("")
                }}>Open Account</button></div>
            </div>
            <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                        <tr>
                            <th>S.No</th>
                            <th>Type</th>
                            <th>Account number</th>
                            <th>Branch</th>
                            <th className="text-end">Balance</th>
                            <th>Opened On</th>
                            <th>Status</th>
                            <th></th>

                        </tr>
                    </thead>
                    <tbody>
                        {
                            filteredAccounts.length === 0 && (
                                <tr><td colSpan="8" className="text-center text-muted py-4">No accounts found</td></tr>
                            )
                        }
                        {
                            filteredAccounts.map((ac, index) => (
                                <tr key={index}>
                                    <td>{index + 1}</td>
                                    <td>{ac.accountType}</td>
                                    <td>{ac.accountNumber}</td>
                                    <td>{ac.branchName}, {ac.district}</td>
                                    <td className="text-end">₹ {ac.balance}</td>
                                    <td>{ac.openedOn}</td>
                                    <td><StatusBadge status={ac.status} /></td>
                                    <td className="text-end text-nowrap">
                                        <button type="button" className="btn btn-sm btn-outline-primary view-acct" data-bs-toggle="modal" data-bs-target="#viewModal" onClick={() => onView(ac)}>View</button>&nbsp;&nbsp;&nbsp;
                                        {ac.status === 'ACTIVE' && <button type="button" className="btn btn-sm btn-outline-danger" data-bs-toggle="modal" data-bs-target="#closeModal" onClick={() => {
                                            setAccountNumber(ac.accountNumber)
                                            setAccountId(ac.id)
                                            setAccountType(ac.accountType)
                                        }}>Close</button>}
                                        {ac.status === 'SUSPENDED' && <span className="text-muted small">Visit the branch to reactivate</span>}
                                    </td>
                                </tr>
                            ))
                        }

                    </tbody>
                </table>
            </div>

            <div className="modal fade" id="viewModal" tabIndex="-1" aria-hidden="true">
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Account Details</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>

                        <div className="modal-body">
                            {viewAcc && viewAcc.status === 'SUSPENDED' && (
                                <div className="alert alert-warning small">
                                    This account is suspended by the bank. Please visit the {viewAcc.branchName.replace(/_/g, ' ')} branch with your ID proof, the bank reactivates it after the verification.
                                </div>
                            )}
                            {viewAcc && (
                                <dl className="row mb-0">
                                    <dt className="col-5">Account holder</dt>
                                    <dd className="col-7">{viewAcc.name}</dd>

                                    <dt className="col-5">Account number</dt>
                                    <dd className="col-7">{viewAcc.accountNumber}</dd>

                                    <dt className="col-5">Account type</dt>
                                    <dd className="col-7">{viewAcc.accountType}</dd>

                                    <dt className="col-5">Status</dt>
                                    <dd className="col-7"><StatusBadge status={viewAcc.status} /></dd>

                                    <dt className="col-5">Balance</dt>
                                    <dd className="col-7">₹{Number(viewAcc.balance).toLocaleString('en-IN')}</dd>

                                    <dt className="col-5">Opened on</dt>
                                    <dd className="col-7">{viewAcc.openedOn}</dd>

                                    <dt className="col-5">Branch</dt>
                                    <dd className="col-7">{viewAcc.branchName}</dd>

                                    <dt className="col-5">District</dt>
                                    <dd className="col-7">{viewAcc.district}</dd>

                                    <dt className="col-5">IFSC code</dt>
                                    <dd className="col-7">{viewAcc.ifscCode}</dd>

                                    <dt className="col-5">Executive</dt>
                                    <dd className="col-7">{viewAcc.executiveName}</dd>

                                    <dt className="col-5">Executive email</dt>
                                    <dd className="col-7">{viewAcc.executiveEmail}</dd>
                                </dl>
                            )}
                            {viewAcc && viewAcc.status === 'ACTIVE' && (
                                <form className="border-top pt-3 mt-3" onSubmit={onSetPassword}>
                                    <label className="form-label fw-semibold mb-0">Transaction password</label>
                                    <div className="form-text mb-2">4 digits, asked for withdraw, transfer and check balance.</div>
                                    <div className="input-group">
                                        <input type="password" className="form-control" required pattern="[0-9]{4}" maxLength={4} placeholder="4 digits" value={txnPassword} onChange={(e) => setTxnPassword(e.target.value)} />
                                        <button type="submit" className="btn btn-maverick">Set</button>
                                    </div>
                                </form>
                            )}
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>

            <div className="modal fade" id="closeModal" tabIndex="-1"><div className="modal-dialog">
                <form className="modal-content needs-validation" data-success="Closure request submitted. A bank employee will review it." onSubmit={(e) => onCloseAccont(e)}>
                    <div className="modal-header">
                        <h2 className="modal-title fs-5">Request account closure</h2>
                        <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div className="modal-body"><label className="form-label">Account</label>
                    <input className="form-control" disabled value={accountNumber}/>
                        <label className="form-label">Reason</label>
                        <textarea className="form-control mb-3" rows="3" required value={reason} onChange={(e) => setReason(e.target.value)}></textarea>
                        <div className="form-check">
                            <input className="form-check-input" checked={agreed} type="checkbox" required id="cc" onChange={(e) => setAgreed(e.target.checked)}/>
                            <label className="form-check-label" required htmlFor="cc">I understand the account will be closed after employee approval.</label>
                        </div>
                        {error && <div className="text-danger mb-3">{error}</div>}
                    </div>
                    <div className="modal-footer"><button type="button" className="btn btn-light" data-bs-dismiss="modal" ref={closeReqRef}>Cancel</button>
                    <button className="btn btn-danger" >Submit request</button>
                    </div></form></div>
            </div>

            <div className="modal fade" id="openAccountModal" tabIndex="-1">
                <div className="modal-dialog modal-dialog-centered">
                    <form className="modal-content" onSubmit={(e) => onOpenAccount(e)}>
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Open new account</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>

                        <div className="modal-body">


                            <div className="mb-3">
                                <label className="form-label">Account type</label>
                                <select className="form-select" required value={newAccountType}
                                    onChange={(e) => setNewAccountType(e.target.value)}>
                                    <option value="">Select type</option>
                                    <option value="SAVINGS">SAVINGS</option>
                                    <option value="CHECKING">CHECKING</option>
                                    <option value="BUSINESS">BUSINESS</option>
                                </select>
                            </div>

                            <div className="mb-3">
                                <label className="form-label">Branch</label>
                                <select className="form-select" required value={branch}
                                    onChange={(e) => setBranch(e.target.value)}>
                                    <option value="">Select branch</option>
                                    {
                                        branchList.map((br, index) => (
                                            <option key={index} value={br.name}>
                                                {br.name}
                                            </option>
                                        ))
                                    }
                                </select>
                            </div>

                            <div className="mb-3">
                                <label className="form-label">Branch District</label>
                                <input type="text" className="form-control" disabled
                                    value={selectedBranch?.district ?? ""} />
                            </div>

                            <div className="mb-3">
                                <label className="form-label">Aadhaar number</label>
                                <input type="text" className="form-control" required
                                    inputMode="numeric" maxLength={12}
                                    placeholder="12-digit Aadhaar number"
                                    title="Enter a valid 12-digit Aadhaar number"
                                    value={aadhaarNumber}
                                    onChange={(e) => setAadhaarNumber(e.target.value)} />
                            </div>

                            <div className="mb-3">
                                <label className="form-label">PAN number</label>
                                <input type="text" className="form-control" required
                                    pattern="[A-Z]{5}[0-9]{4}[A-Z]" maxLength={10}
                                    value={panNumber}
                                    onChange={(e) => setPanNumber(e.target.value.toUpperCase())} />
                            </div>

                            <div className="mb-3">
                                <label className="form-label">Initial deposit (₹)</label>
                                <input type="number" className="form-control" required min="0" step="0.01"
                                    value={initialDeposit}
                                    onChange={(e) => setInitialDeposit(e.target.value)} />
                            </div>

                            <div className="mb-3">
                                <label className="form-label">Aadhaar document</label>
                                <input type="file" className="form-control" required
                                    accept=".jpg,.jpeg,.png,.pdf"
                                    onChange={(e) => setAadhaarFile(e.target.files[0])} />
                            </div>

                            <div className="mb-3">
                                <label className="form-label">PAN document</label>
                                <input type="file" className="form-control" required
                                    accept=".jpg,.jpeg,.png,.pdf"
                                    onChange={(e) => setPanFile(e.target.files[0])} />
                            </div>

                            <div className="mb-3">
                                <label className="form-label">Photo document</label>
                                <input type="file" className="form-control" required
                                    accept=".jpg,.jpeg,.png,.pdf"
                                    onChange={(e) => setPhotoFile(e.target.files[0])} />
                            </div>

                            <div className="mb-0">
                                <label className="form-label">Signature document</label>
                                <input type="file" className="form-control" required
                                    accept=".jpg,.jpeg,.png,.pdf"
                                    onChange={(e) => setSignatureFile(e.target.files[0])} />
                            </div>


                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal" ref={openReqRef}>Cancel</button>
                            <button type="submit" className="btn btn-maverick" >Submit request</button>
                        </div>
                    </form>
                </div>
            </div>

        </div>
    )
}


export default AccountList