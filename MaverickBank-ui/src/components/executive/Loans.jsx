import axios from "axios"
import { useEffect, useRef, useState } from "react"
import { useSearchParams } from "react-router"
import { API_SERVER_URL_DEV } from "../../config"
import StatusBadge from "../common/StatusBadge"
import Pagination from "../common/Pagination"

function Loans({ manager = false, admin = false }) {

    const [branches, setBranches] = useState([])
    const [team, setTeam] = useState([])
    const [teamReload, setTeamReload] = useState(0)

    const [searchParams] = useSearchParams()
    const statusParam = searchParams.get("status") ?? ""

    const [loans, setLoans] = useState([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalRecords, setTotalRecords] = useState(0)
    const [recordsPerPage, setRecordsPerPage] = useState(0)
    const [page, setPage] = useState(0)
    const size = 7
    const [reload, setReload] = useState(0)

    const [unassigned, setUnassigned] = useState(manager && !statusParam)
    const [assignee, setAssignee] = useState("")
    const [branchName, setBranchName] = useState("")
    const [loanStatus, setLoanStatus] = useState(statusParam || "IN_PROCESS")
    const [loanType, setLoanType] = useState("")
    const [accountNumber, setAccountNumber] = useState("")
    const [nameInput, setNameInput] = useState("")
    const [customerName, setCustomerName] = useState("")
    const [requestedFrom, setRequestedFrom] = useState("")
    const [requestedTo, setRequestedTo] = useState("")

    const [viewLoan, setViewLoan] = useState(null)
    const [amount, setAmount] = useState("")
    const [interestRate, setInterestRate] = useState("")
    const [tenureMonths, setTenureMonths] = useState("")
    const closeRef = useRef(null)

    useEffect(() => {
        if (!admin) return
        const getBranches = async () => {
            try {
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/branch/all`)
                setBranches(response.data)
            }
            catch (err) {
                console.log("could not load the branches", err?.response?.data)
            }
        }
        getBranches()
    }, [admin])

    useEffect(() => {
        if (!manager) return
        const getTeam = async () => {
            try {
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/executive/team`, {
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
                })
                setTeam(response.data)
            }
            catch (err) {
                console.log("could not load the team", err?.response?.data)
            }
        }
        getTeam()
    }, [manager, teamReload])

    useEffect(() => {
        const getLoans = async () => {
            try {
                const params = { page, size, unassigned }
                if (branchName) params.branchName = branchName
                if (loanStatus) params.loanStatus = loanStatus
                if (loanType) params.loanType = loanType
                if (accountNumber) params.accountNumber = accountNumber
                if (customerName) params.customerName = customerName
                if (requestedFrom) params.requestedFrom = requestedFrom
                if (requestedTo) params.requestedTo = requestedTo

                const response = await axios.get(`${API_SERVER_URL_DEV}/api/loan/filter`, {
                    params,
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
                })

                setLoans(response.data.list)
                setTotalPages(response.data.paginationInfo.totalPages)
                setTotalRecords(response.data.paginationInfo.totalRecords)
                setRecordsPerPage(response.data.paginationInfo.recordsPerPage)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load loans",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        getLoans()
    }, [page, size, unassigned, branchName, loanStatus, loanType, accountNumber, customerName, requestedFrom, requestedTo, reload])

    const onSearch = (e) => {
        e.preventDefault()
        setCustomerName(nameInput.trim())
        setPage(0)
    }

    const onClear = () => {
        setUnassigned(manager)
        setBranchName("")
        setLoanStatus("IN_PROCESS")
        setLoanType("")
        setAccountNumber("")
        setNameInput("")
        setCustomerName("")
        setRequestedFrom("")
        setRequestedTo("")
        setPage(0)
    }

    const canSanction = (l) => !manager && !admin && l.loanStatus === 'IN_PROCESS'
    const canDisburse = (l) => !manager && !admin && l.loanStatus === 'VERIFIED'

    const canAssign = (l) => manager && l.loanStatus === 'IN_PROCESS' && !l.executiveName
    const canVerify = (l) => manager && l.loanStatus === 'ACCEPTED'

    const onView = (l) => {
        setViewLoan(l)
        setAssignee("")
        setAmount(l.amount)
        setInterestRate(l.interestRate ?? "")
        setTenureMonths(l.tenureMonths)
    }

    const onSanction = async (e) => {
        e.preventDefault()
        const result = await window.Swal.fire({
            icon: "question",
            title: "Sanction the loan?",
            showCancelButton: true,
            confirmButtonText: "Sanction"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/approve/${viewLoan.id}`, { amount, interestRate, tenureMonths }, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Loan sanctioned, waiting for the customer",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not sanction",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onReject = async () => {
        const result = await window.Swal.fire({
            title: "Reject the loan",
            input: "textarea",
            inputLabel: "Reason, the customer will see it",
            inputPlaceholder: "Reason for rejection",
            showCancelButton: true,
            confirmButtonText: "Reject",
            confirmButtonColor: "#dc3545",
            inputValidator: (value) => !value && "Reason is required"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/reject/${viewLoan.id}`, { reason: result.value }, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Loan rejected",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not reject",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onDisburse = async () => {
        const result = await window.Swal.fire({
            icon: "question",
            title: "Disburse the loan?",
            text: `₹ ${viewLoan.amount} will be credited to account ${viewLoan.accountNumber}`,
            showCancelButton: true,
            confirmButtonText: "Disburse"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/disburse/${viewLoan.id}`, null, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Loan disbursed",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not disburse",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onAssign = async () => {
        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan-request/assign-executive/${viewLoan.id}`, { executiveId: assignee }, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Loan assigned",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
            setTeamReload(teamReload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not assign",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onVerify = async () => {
        const result = await window.Swal.fire({
            icon: "question",
            title: "Verify the loan?",
            html: `Amount ₹ ${viewLoan.amount}<br/>Interest ${viewLoan.interestRate}% per year<br/>Tenure ${viewLoan.tenureMonths} months<br/>Sanctioned by ${viewLoan.executiveName ?? "-"}`,
            showCancelButton: true,
            confirmButtonText: "Verify"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/verify/${viewLoan.id}`, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Loan verified, the executive can disburse it",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
            setTeamReload(teamReload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not verify",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onRejectVerification = async () => {
        const result = await window.Swal.fire({
            title: "Reject the loan",
            input: "textarea",
            inputLabel: "Reason, the customer will see it",
            inputPlaceholder: "Reason for rejection",
            showCancelButton: true,
            confirmButtonText: "Reject",
            confirmButtonColor: "#dc3545",
            inputValidator: (value) => !value && "Reason is required"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/loan/reject-verification/${viewLoan.id}`, { reason: result.value }, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Loan rejected",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
            setTeamReload(teamReload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not reject",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    return (
        <div>
            <div className="card mb-4">
                <div className="card-body">
                    <form className="row g-3 align-items-end" onSubmit={onSearch}>
                        <div className="col-md-2">
                            <label className="form-label">Status</label>
                            <select className="form-select" value={loanStatus} onChange={(e)=>{
                                setLoanStatus(e.target.value)
                                setPage(0)
                            }}>
                                <option value="">All</option>
                                <option value="IN_PROCESS">IN PROCESS</option>
                                <option value="SANCTIONED">SANCTIONED</option>
                                <option value="ACCEPTED">ACCEPTED</option>
                                <option value="VERIFIED">VERIFIED</option>
                                <option value="OPEN">OPEN</option>
                                <option value="CLOSED">CLOSED</option>
                                <option value="REJECTED">REJECTED</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Type</label>
                            <select className="form-select" value={loanType} onChange={(e)=>{
                                setLoanType(e.target.value)
                                setPage(0)
                            }}>
                                <option value="">All</option>
                                <option value="PERSONAL">PERSONAL</option>
                                <option value="HOUSE">HOUSE</option>
                                <option value="BUSINESS">BUSINESS</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Account number</label>
                            <input type="number" className="form-control" value={accountNumber} onChange={(e)=>{
                                setAccountNumber(e.target.value)
                                setPage(0)
                            }} />
                        </div>
                        <div className="col-md-3">
                            <label className="form-label">Customer name</label>
                            <div className="input-group">
                                <input type="text" className="form-control" placeholder="Search" value={nameInput} onChange={(e) => setNameInput(e.target.value)} />
                                <button type="submit" className="btn btn-outline-secondary"><i className="bi bi-search"></i></button>
                            </div>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Requested from</label>
                            <input type="date" className="form-control" value={requestedFrom} onChange={(e)=>{
                                setRequestedFrom(e.target.value)
                                setPage(0)
                            }} />
                        </div>
                        <div className="col-md-1 d-grid">
                            <button type="button" className="btn btn-outline-secondary" onClick={onClear}>Clear</button>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Requested to</label>
                            <input type="date" className="form-control" value={requestedTo} onChange={(e)=>{
                                setRequestedTo(e.target.value)
                                setPage(0)
                            }} />
                        </div>
                        {admin && (
                            <div className="col-md-3">
                                <label className="form-label">Branch</label>
                                <select className="form-select" value={branchName} onChange={(e)=>{
                                setBranchName(e.target.value)
                                setPage(0)
                            }}>
                                    <option value="">All branches</option>
                                    {branches.map(b => <option key={b.id} value={b.name}>{b.name}</option>)}
                                </select>
                            </div>
                        )}
                        {manager && (
                            <div className="col-md-10 d-flex align-items-end">
                                <div className="form-check form-switch">
                                    <input className="form-check-input" type="checkbox" id="unassignedSwitch" checked={unassigned} onChange={(e) => { setUnassigned(e.target.checked); setPage(0) }} />
                                    <label className="form-check-label" htmlFor="unassignedSwitch">{unassigned ? "Unassigned only" : "Whole branch"}</label>
                                </div>
                            </div>
                        )}
                    </form>
                </div>
            </div>

            <div className="card">
                <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-light">
                            <tr>
                                <th>S.No</th>
                                <th>ID</th>
                                <th>Customer</th>
                                <th>Account</th>
                                <th>Type</th>
                                <th className="text-end">Amount</th>
                                <th>Rate</th>
                                <th>Tenure</th>
                                <th>Executive</th>
                                <th>Requested on</th>
                                <th>Status</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {loans.length === 0 && <tr><td colSpan="12" className="text-center text-muted py-4">No loans found</td></tr>}
                            {loans.map((l, index) => (
                                <tr key={index}>
                                    <td>{page * size + index + 1}</td>
                                    <td>#{l.id}</td>
                                    <td>{l.customerName}</td>
                                    <td>{l.accountNumber}</td>
                                    <td>{l.loanType}</td>
                                    <td className="text-end">₹ {l.amount}</td>
                                    <td>{l.interestRate ? l.interestRate + '%' : '-'}</td>
                                    <td>{l.tenureMonths} months</td>
                                    <td>{l.executiveName ? l.executiveName : <span className="text-muted">Unassigned</span>}</td>
                                    <td>{l.requestedOn}</td>
                                    <td><StatusBadge status={l.loanStatus} /></td>
                                    <td className="text-end text-nowrap">
                                        <button type="button" className={`btn btn-sm ${canSanction(l) || canDisburse(l) || canAssign(l) || canVerify(l) ? "btn-maverick" : "btn-outline-primary"}`} data-bs-toggle="modal" data-bs-target="#loanModal" onClick={() => onView(l)}>
                                            {canAssign(l) ? "Assign" : canVerify(l) ? "Verify" : canSanction(l) ? "Review" : canDisburse(l) ? "Disburse" : "View"}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                <div className="card-footer bg-white d-flex flex-wrap justify-content-center align-items-center gap-4">
                
                    <Pagination page={page} totalPages={totalPages} totalRecords={totalRecords} recordsPerPage={recordsPerPage} onPageChange={setPage} />
                </div>
            </div>

            <div className="modal fade" id="loanModal" tabIndex="-1" aria-hidden="true" data-bs-focus="false">
                <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Loan #{viewLoan?.id}</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal" ref={closeRef}></button>
                        </div>

                        <div className="modal-body">
                            {viewLoan && (
                                <div className="row g-4">
                                    <div className={canSanction(viewLoan) ? "col-md-6" : "col-12"}>
                                        <h6 className="fw-semibold">Loan request</h6>
                                        <dl className="row mb-0 small">
                                            <dt className="col-5">Customer</dt><dd className="col-7">{viewLoan.customerName}</dd>
                                            <dt className="col-5">Account</dt><dd className="col-7">{viewLoan.accountNumber}</dd>
                                            <dt className="col-5">Type</dt><dd className="col-7">{viewLoan.loanType}</dd>
                                            <dt className="col-5">Purpose</dt><dd className="col-7">{viewLoan.purpose ?? "-"}</dd>
                                            <dt className="col-5">Requested amount</dt><dd className="col-7">₹ {viewLoan.amount}</dd>
                                            <dt className="col-5">Interest rate</dt><dd className="col-7">{viewLoan.interestRate ? viewLoan.interestRate + '%' : '-'}</dd>
                                            <dt className="col-5">Tenure</dt><dd className="col-7">{viewLoan.tenureMonths} months</dd>
                                            <dt className="col-5">Status</dt><dd className="col-7"><StatusBadge status={viewLoan.loanStatus} /></dd>
                                            <dt className="col-5">Outstanding</dt><dd className="col-7">{viewLoan.outstandingAmount ? `₹ ${viewLoan.outstandingAmount}` : '-'}</dd>
                                            <dt className="col-5">Requested on</dt><dd className="col-7">{viewLoan.requestedOn}</dd>
                                            <dt className="col-5">Processed on</dt><dd className="col-7">{viewLoan.processedOn ?? "-"}</dd>
                                            <dt className="col-5">Disbursed on</dt><dd className="col-7">{viewLoan.disbursedDate ?? "-"}</dd>
                                            <dt className="col-5">Executive</dt><dd className="col-7">{viewLoan.executiveName ?? "Unassigned"}</dd>
                                            <dt className="col-5">Remark</dt><dd className="col-7">{viewLoan.remark ?? "-"}</dd>
                                        </dl>
                                    </div>
                                    {canSanction(viewLoan) && (
                                        <div className="col-md-6">
                                            <h6 className="fw-semibold">Sanction terms</h6>
                                            <form id="sanctionForm" onSubmit={onSanction}>
                                                <div className="mb-3">
                                                    <label className="form-label">Amount (₹)</label>
                                                    <input type="number" className="form-control" required min="1" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
                                                </div>
                                                <div className="mb-3">
                                                    <label className="form-label">Interest rate (% per year)</label>
                                                    <input type="number" className="form-control" required min="0.01" step="0.01" value={interestRate} onChange={(e) => setInterestRate(e.target.value)} />
                                                </div>
                                                <div className="mb-3">
                                                    <label className="form-label">Tenure (months)</label>
                                                    <input type="number" className="form-control" required min="1" value={tenureMonths} onChange={(e) => setTenureMonths(e.target.value)} />
                                                </div>
                                            </form>
                                        </div>
                                    )}
                                    {(viewLoan.loanStatus === 'SANCTIONED' || viewLoan.loanStatus === 'ACCEPTED') && (
                                        <div className="col-12">
                                            <div className="alert alert-info mb-0 small">
                                                {viewLoan.loanStatus === 'SANCTIONED' ? "Waiting for the customer to accept the offer." : "Accepted by the customer, waiting for the manager's verification."}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        <div className="modal-footer">
                            {viewLoan && canAssign(viewLoan) && (
                                <div className="d-flex align-items-center gap-2 me-auto">
                                    <select className="form-select form-select-sm w-auto" value={assignee} onChange={(e) => setAssignee(e.target.value)}>
                                        <option value="">Select executive</option>
                                        {team.map(t => <option key={t.id} value={t.id}>{t.name} ({t.pendingRequests + t.pendingLoans} open)</option>)}
                                    </select>
                                    <button type="button" className="btn btn-maverick btn-sm" onClick={onAssign} disabled={!assignee}>Assign</button>
                                </div>
                            )}
                            {viewLoan && canVerify(viewLoan) && (
                                <>
                                    <button type="button" className="btn btn-outline-danger" onClick={onRejectVerification}>Reject</button>
                                    <button type="button" className="btn btn-maverick" onClick={onVerify}>Verify</button>
                                </>
                            )}
                            {viewLoan && canSanction(viewLoan) && (
                                <>
                                    <button type="button" className="btn btn-outline-danger" onClick={onReject}>Reject</button>
                                    <button type="submit" form="sanctionForm" className="btn btn-maverick">Sanction</button>
                                </>
                            )}
                            {viewLoan && canDisburse(viewLoan) && (
                                <button type="button" className="btn btn-maverick" onClick={onDisburse}>Disburse</button>
                            )}
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Loans
