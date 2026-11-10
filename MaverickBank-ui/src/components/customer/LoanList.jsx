import axios from "axios"
import { useState } from "react"
import { useDispatch, useSelector } from "react-redux"
import { API_SERVER_URL_DEV } from "../../config"
import { getAccounts, getLoans, getTransactions } from "../../store/actions/CustomerActions"
import StatusBadge from "../common/StatusBadge"

function LoanList() {
    const loanList = useSelector(state => state.customerLoans)
    const dispatch = useDispatch()
    const headers = { Authorization: 'Bearer ' + localStorage.getItem('token') }
    const [viewLoan, setViewLoan] = useState(null)

   
    const [statusFilter, setStatusFilter] = useState("ALL")
    const filteredLoans = statusFilter === "ALL" ? loanList : loanList.filter(l => l.loanStatus === statusFilter)

    
    const onAccept = async (l) => {
        const result = await window.Swal.fire({
            icon: "question",
            title: "Accept the loan offer?",
            showCancelButton: true,
            confirmButtonText: "Accept"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/accept/${l.id}`, null, { headers })
            window.Swal.fire({
                icon: "success",
                title: "Offer accepted, the bank will verify and disburse",
                showConfirmButton: false,
                timer: 1800
            })
            dispatch(getLoans())
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not accept",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    
    const onRepay = async (l) => {
        const result = await window.Swal.fire({
            title: "Repay the loan",
            input: "number",
            inputLabel: "Enter the Amount to pay",
            inputAttributes: { min: 1, step: 0.01 },
            showCancelButton: true,
            confirmButtonText: "Repay",
            inputValidator: (value) => {
                if (!value || Number(value) <= 0) return "Amount is required"
                if (Number(value) > Number(l.outstandingAmount)) return "Amount exceeds the outstanding amount"
            }
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/repay/${l.id}`, { amount: result.value }, { headers })
            window.Swal.fire({
                icon: "success",
                title: "Repayment done",
                showConfirmButton: false,
                timer: 1500
            })
            
            dispatch(getLoans())
            dispatch(getAccounts())
            dispatch(getTransactions())
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not repay",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onDecline = async (l) => {
        const result = await window.Swal.fire({
            title: "Decline the loan offer",
            input: "textarea",
            inputLabel: "Reason",
            inputPlaceholder: "Why are you declining?",
            showCancelButton: true,
            confirmButtonText: "Decline",
            confirmButtonColor: "#dc3545",
            inputValidator: (value) => !value && "Reason is required"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/decline/${l.id}`, { reason: result.value }, { headers })
            window.Swal.fire({
                icon: "success",
                title: "Offer declined",
                showConfirmButton: false,
                timer: 1500
            })
            dispatch(getLoans())
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not decline",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onView = (l) => {
        let obj = {
            accountNumber: l.accountNumber,
            loanType: l.loanType,
            amount: l.amount,
            interestRate: l.interestRate,
            tenureMonths: l.tenureMonths,
            purpose : l.purpose,
            loanStatus: l.loanStatus,
            outstandingAmount: l.outstandingAmount,
            requestedOn: l.requestedOn,
            processedOn: l.processedOn,
            disbursedDate: l.disbursedDate,
            closedOn : l.closedOn,
            remark : l.remark,
            executiveName: l.executiveName,
            executiveEmail: l.executiveEmail
        }

        setViewLoan(obj)
    }
    
    return (
        <div className="card">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <div className="fw-semibold">My loans</div>
                <select className="form-select form-select-sm w-auto" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                    <option value="ALL">All status</option>
                    <option value="IN_PROCESS">IN PROCESS</option>
                    <option value="SANCTIONED">SANCTIONED</option>
                    <option value="ACCEPTED">ACCEPTED</option>
                    <option value="VERIFIED">VERIFIED</option>
                    <option value="OPEN">OPEN</option>
                    <option value="CLOSED">CLOSED</option>
                    <option value="REJECTED">REJECTED</option>
                </select>
            </div>
            <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                        <tr>
                            <th>S.No</th>
                            <th>Loan</th>
                            <th>Amount</th>
                            <th>Interest rate</th>
                            <th>Tenure</th>
                            <th>Executive</th>
                            <th>Requested On</th>
                            <th>Status</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            filteredLoans.length === 0 && (
                                <tr><td colSpan="9" className="text-center text-muted py-4">No loans found</td></tr>
                            )
                        }
                        {
                            filteredLoans.map((l,index) => (
                                <tr key={index}>
                                    <td>{index+1}</td>
                                    <td>{l.loanType}</td>
                                    <td>₹{l.amount}</td>
                                    <td>{l.interestRate ? l.interestRate + '%' : '-'}</td>
                                    <td>{l.tenureMonths} months</td>
                                    <td>
                                        {l.executiveName ? (
                                        <>
                                            <div>{l.executiveName}</div>
                                            <small className="text-muted">{l.executiveEmail}</small>
                                        </>
                                    ) : (
                                        <span className="text-muted">Unassigned</span>
                                    )}
                                    </td>
                                    <td>{l.requestedOn}</td>
                                    <td><StatusBadge status={l.loanStatus} /></td>
                                    <td className="text-end text-nowrap">
                                        <div className="d-flex flex-column align-items-end gap-1">
                                            <button type="button" className="btn btn-sm btn-outline-primary view-acct" data-bs-toggle="modal" data-bs-target="#viewModal" onClick={() => onView(l)}>View</button>
                                            {l.loanStatus === 'SANCTIONED' && (
                                                <div className="btn-group btn-group-sm">
                                                    <button type="button" className="btn btn-success" onClick={() => onAccept(l)}>Accept</button>
                                                    <button type="button" className="btn btn-outline-danger" onClick={() => onDecline(l)}>Decline</button>
                                                </div>
                                            )}
                                            {l.loanStatus === 'OPEN' && (
                                                <button type="button" className="btn btn-sm btn-maverick" onClick={() => onRepay(l)}>Repay</button>
                                            )}
                                        </div>
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
                            <h2 className="modal-title fs-5">Loan Details</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>

                        <div className="modal-body">
                            {viewLoan && (
                                <dl className="row mb-0">
                                    <dt className="col-5">Account number</dt>
                                    <dd className="col-7">{viewLoan.accountNumber}</dd>

                                    <dt className="col-5">Loan type</dt>
                                    <dd className="col-7">{viewLoan.loanType}</dd>

                                    <dt className="col-5">Status</dt>
                                    <dd className="col-7"><StatusBadge status={viewLoan.loanStatus} /></dd>

                                    <dt className="col-5">Amount</dt>
                                    <dd className="col-7">₹ {viewLoan.amount}</dd>

                                    <dt className="col-5">Interest Rate</dt>
                                    <dd className="col-7">{viewLoan.interestRate ? viewLoan.interestRate + '%' : '-'}</dd>

                                    <dt className="col-5">Tenure Months</dt>
                                    <dd className="col-7">{viewLoan.tenureMonths}</dd>

                                    <dt className="col-5">Purpose</dt>
                                    <dd className="col-7">{viewLoan.purpose}</dd>

                                    <dt className="col-5">Outstanding Amount</dt>
                                    <dd className="col-7">{viewLoan.outstandingAmount ? '₹ ' + viewLoan.outstandingAmount : '-'}</dd>

                                    <dt className="col-5">Requested on</dt>
                                    <dd className="col-7">{viewLoan.requestedOn ? viewLoan.requestedOn : '-'}</dd>

                                    <dt className="col-5">Processed on</dt>
                                    <dd className="col-7">{viewLoan.processedOn ? viewLoan.processedOn : '-'}</dd>

                                    <dt className="col-5">Disbursed Date</dt>
                                    <dd className="col-7">{viewLoan.disbursedDate ? viewLoan.disbursedDate : '-'}</dd>

                                    <dt className="col-5">Closed On</dt>
                                    <dd className="col-7">{viewLoan.closedOn ? viewLoan.closedOn : '-'}</dd>

                                    <dt className="col-5">Remark</dt>
                                    <dd className="col-7">{viewLoan.remark ? viewLoan.remark : '-'}</dd>

                                    <dt className="col-5">Executive</dt>
                                    <dd className="col-7">{viewLoan.executiveName ? viewLoan.executiveName : 'Unassigned'}</dd>

                                    <dt className="col-5">Executive email</dt>
                                    <dd className="col-7">{viewLoan.executiveEmail ? viewLoan.executiveEmail : '-'}</dd>
                                </dl>
                            )}
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default LoanList