import axios from "axios"
import { useEffect, useRef, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import StatusBadge from "../common/StatusBadge"
import Pagination from "../common/Pagination"
import KycDocument from "../common/KycDocument"

function AccountRequests({ manager = false, admin = false }) {

    const [branches, setBranches] = useState([])
    const [team, setTeam] = useState([])
    const [teamReload, setTeamReload] = useState(0)

    const [requests, setRequests] = useState([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalRecords, setTotalRecords] = useState(0)
    const [recordsPerPage, setRecordsPerPage] = useState(0)
    const [page, setPage] = useState(0)
    const size = 7
    const [reload, setReload] = useState(0)

    const [unassigned, setUnassigned] = useState(manager)
    const [assignee, setAssignee] = useState("")
    const [branchName, setBranchName] = useState("")
    const [requestType, setRequestType] = useState("")
    const [requestStatus, setRequestStatus] = useState("IN_PROCESS")
    const [nameInput, setNameInput] = useState("")
    const [customerName, setCustomerName] = useState("")
    const [requestedFrom, setRequestedFrom] = useState("")
    const [requestedTo, setRequestedTo] = useState("")

    const [viewReq, setViewReq] = useState(null)
    const [customer, setCustomer] = useState(null)
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
                    headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
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
        const getRequests = async () => {
            try {
                const params = { page, size, unassigned }
                if (branchName) params.branchName = branchName
                if (requestType) params.requestType = requestType
                if (requestStatus) params.requestStatus = requestStatus
                if (customerName) params.customerName = customerName
                if (requestedFrom) params.requestedFrom = requestedFrom
                if (requestedTo) params.requestedTo = requestedTo

                const response = await axios.get(`${API_SERVER_URL_DEV}/api/account-request/filter`, { params, 
                    headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
                 })

                setRequests(response.data.list)
                setTotalPages(response.data.paginationInfo.totalPages)
                setTotalRecords(response.data.paginationInfo.totalRecords)
                setRecordsPerPage(response.data.paginationInfo.recordsPerPage)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load requests",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        getRequests()
    }, [page, size, unassigned, branchName, requestType, requestStatus, customerName, requestedFrom, requestedTo, reload])


    const onSearch = (e) => {
        e.preventDefault()
        setCustomerName(nameInput.trim())
        setPage(0)
    }

    const onClear = () => {
        setUnassigned(manager)
        setBranchName("")
        setRequestType("")
        setRequestStatus("IN_PROCESS")
        setNameInput("")
        setCustomerName("")
        setRequestedFrom("")
        setRequestedTo("")
        setPage(0)
    }


    const canAct = (req) => !manager && !admin && req.requestStatus === 'IN_PROCESS'

    const canAssign = (req) => manager && req.requestStatus === 'IN_PROCESS' && !req.executiveName

    const onView = async (req) => {
        setViewReq(req)
        setCustomer(null)
        setAssignee("")
        try {
            const response = await axios.get(`${API_SERVER_URL_DEV}/api/customer/${req.customerId}`,null, { 
                headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
             })
            setCustomer(response.data)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not load the customer",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onVerifyKyc = async () => {
        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/customer/kyc-update/${viewReq.customerId}`,null, { 
                headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
             })
            setCustomer({ ...customer, kycStatus: 'DONE' })
            window.Swal.fire({
                icon: "success",
                title: "KYC verified",
                showConfirmButton: false,
                timer: 1500
            })
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not verify KYC",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onApprove = async () => {
        const result = await window.Swal.fire({
            icon: "question",
            title: viewReq.requestType === 'OPEN' ? "Open the account?" : "Close the account?",
            text: viewReq.requestType === 'OPEN'
                ? `A ${viewReq.accountType} account will be opened for ${viewReq.customerName} in ${viewReq.branchName}`
                : "The account will be closed permanently",
            showCancelButton: true,
            confirmButtonText: "Approve"
        })
        if (!result.isConfirmed) return

        const path = viewReq.requestType === 'OPEN' ? 'open-request/approve' : 'close-request/approve'
        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/account/${path}/${viewReq.id}`, null, { 
                headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
             })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Request approved",
                showConfirmButton: false,
                timer: 1500
            })

            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not approve",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onReject = async () => {
        const result = await window.Swal.fire({
            title: "Reject the request",
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
            await axios.put(`${API_SERVER_URL_DEV}/api/account/request/reject/${viewReq.id}`, { reason: result.value }, { 
                headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
             })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Request rejected",
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

    const onAssign = async () => {
        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/request/assign-executive/${viewReq.id}`, { executiveId: assignee }, { 
                headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
             })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Request assigned",
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

    return (
        <div>
            <div className="card mb-4">
                <div className="card-body">
                    <form className="row g-3 align-items-end" onSubmit={(e)=>onSearch(e)}>
                        <div className="col-md-2">
                            <label className="form-label">Status</label>
                            <select className="form-select" value={requestStatus} onChange={(e) => { setRequestStatus(e.target.value); setPage(0) }}>
                                <option value="">All</option>
                                <option value="IN_PROCESS">IN PROCESS</option>
                                <option value="APPROVED">APPROVED</option>
                                <option value="REJECTED">REJECTED</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Type</label>
                            <select className="form-select" value={requestType} onChange={(e) => { setRequestType(e.target.value); setPage(0) }}>
                                <option value="">All</option>
                                <option value="OPEN">OPEN</option>
                                <option value="CLOSE">CLOSE</option>
                            </select>
                        </div>
                        <div className="col-md-3">
                            <label className="form-label">Customer name</label>
                            <div className="input-group">
                                <input type="text" className="form-control" placeholder="Search" value={nameInput} onChange={(e) => setNameInput(e.target.value)} />
                                <button type="submit" className="btn btn-outline-secondary"><i className="bi bi-search"></i></button>
                            </div>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">From</label>
                            <input type="date" className="form-control" value={requestedFrom} onChange={(e) => { setRequestedFrom(e.target.value); setPage(0) }} />
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">To</label>
                            <input type="date" className="form-control" value={requestedTo} onChange={(e) => { setRequestedTo(e.target.value); setPage(0) }} />
                        </div>
                        <div className="col-md-1 d-grid">
                            <button type="button" className="btn btn-outline-secondary" onClick={onClear}>Clear</button>
                        </div>
                        {admin && (
                            <div className="col-md-3">
                                <label className="form-label">Branch</label>
                                <select className="form-select" value={branchName} onChange={(e) => { setBranchName(e.target.value); setPage(0) }}>
                                    <option value="">All branches</option>
                                    {branches.map(b => <option key={b.id} value={b.name}>{b.name}</option>)}
                                </select>
                            </div>
                        )}
                        {manager && (
                            <div className="col-12">
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
                                <th>Request</th>
                                <th>Account type</th>
                                <th>Branch</th>
                                <th>Executive</th>
                                <th>Requested on</th>
                                <th>Status</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {requests.length === 0 && <tr><td colSpan="10" className="text-center text-muted py-4">No requests found</td></tr>}
                            {requests.map((req, index) => (
                                <tr key={index}>
                                    <td>{page * size + index + 1}</td>
                                    <td>#{req.id}</td>
                                    <td>{req.customerName}</td>
                                    <td>{req.requestType}</td>
                                    <td>{req.accountType}</td>
                                    <td>{req.branchName ? req.branchName.replace(/_/g, ' ') : "-"}</td>
                                    <td>{req.executiveName ? req.executiveName : <span className="text-muted">Unassigned</span>}</td>
                                    <td>{req.requestedOn}</td>
                                    <td><StatusBadge status={req.requestStatus} /></td>
                                    <td className="text-end text-nowrap">
                                        <button type="button" className={`btn btn-sm ${canAct(req) || canAssign(req) ? "btn-maverick" : "btn-outline-primary"}`} data-bs-toggle="modal" data-bs-target="#reqModal" onClick={() => onView(req)}>
                                            {canAssign(req) ? "Assign" : canAct(req) ? "Review" : "View"}
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

            <div className="modal fade" id="reqModal" tabIndex="-1" aria-hidden="true" data-bs-focus="false">
                <div className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Request #{viewReq?.id}</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal" ref={closeRef}></button>
                        </div>

                        <div className="modal-body">
                            {viewReq && (
                                <div className="row g-4">
                                    <div className="col-md-6">
                                        <h6 className="fw-semibold">Request</h6>
                                        <dl className="row mb-0 small">
                                            <dt className="col-5">Type</dt><dd className="col-7">{viewReq.requestType}</dd>
                                            <dt className="col-5">Account type</dt><dd className="col-7">{viewReq.accountType}</dd>
                                            <dt className="col-5">Branch</dt><dd className="col-7">{viewReq.branchName ? viewReq.branchName.replace(/_/g, ' ') : "-"}</dd>
                                            <dt className="col-5">Status</dt><dd className="col-7"><StatusBadge status={viewReq.requestStatus} /></dd>
                                            <dt className="col-5">Requested on</dt><dd className="col-7">{viewReq.requestedOn}</dd>
                                            <dt className="col-5">Processed on</dt><dd className="col-7">{viewReq.processedOn ?? "-"}</dd>
                                            <dt className="col-5">Initial deposit</dt><dd className="col-7">{viewReq.initialDeposit === null ? "-" : `₹ ${viewReq.initialDeposit}`}</dd>
                                            <dt className="col-5">Reason</dt><dd className="col-7">{viewReq.reason ?? "-"}</dd>
                                            <dt className="col-5">Executive</dt><dd className="col-7">{viewReq.executiveName ?? "Unassigned"}</dd>
                                            <dt className="col-5">Remark</dt><dd className="col-7">{viewReq.remark ?? "-"}</dd>
                                        </dl>
                                    </div>
                                    <div className="col-md-6">
                                        <h6 className="fw-semibold">Customer</h6>
                                        {customer ? (
                                            <dl className="row mb-0 small">
                                                <dt className="col-5">Name</dt><dd className="col-7">{customer.name}</dd>
                                                <dt className="col-5">Date of birth</dt><dd className="col-7">{customer.dob}</dd>
                                                <dt className="col-5">Gender</dt><dd className="col-7">{customer.gender ?? "-"}</dd>
                                                <dt className="col-5">Contact</dt><dd className="col-7">{customer.contact}</dd>
                                                <dt className="col-5">Email</dt><dd className="col-7">{customer.email ?? "-"}</dd>
                                                <dt className="col-5">Address</dt><dd className="col-7">{customer.address}</dd>
                                                <dt className="col-5">Aadhaar</dt><dd className="col-7">{customer.aadhaarNumber ?? "-"}</dd>
                                                <dt className="col-5">PAN</dt><dd className="col-7">{customer.panNumber ?? "-"}</dd>
                                                <dt className="col-5">KYC</dt><dd className="col-7"><StatusBadge status={customer.kycStatus} /></dd>
                                            </dl>
                                        ) : <span className="text-muted small">Loading...</span>}
                                    </div>
                                    {customer && (
                                        <div className="col-12">
                                            <h6 className="fw-semibold">KYC documents</h6>
                                            <div className="row g-3">
                                                <KycDocument label="Aadhaar" file={customer.aadhaarUrl} />
                                                <KycDocument label="PAN" file={customer.panUrl} />
                                                <KycDocument label="Photo" file={customer.photoUrl} />
                                                <KycDocument label="Signature" file={customer.signatureUrl} />
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        <div className="modal-footer">
                            {viewReq && canAssign(viewReq) && (
                                <div className="d-flex align-items-center gap-2 me-auto">
                                    <select className="form-select form-select-sm w-auto" value={assignee} onChange={(e) => setAssignee(e.target.value)}>
                                        <option value="">Select executive</option>
                                        {team.map(t => <option key={t.id} value={t.id}>{t.name} ({t.pendingRequests + t.pendingLoans} open)</option>)}
                                    </select>
                                    <button type="button" className="btn btn-maverick btn-sm" onClick={onAssign} disabled={!assignee}>Assign</button>
                                </div>
                            )}
                            {viewReq && canAct(viewReq) && (
                                <>
                                    {customer?.kycStatus === 'PENDING' && <button type="button" className="btn btn-outline-primary" onClick={()=>onVerifyKyc()}>Verify KYC</button>}
                                    <button type="button" className="btn btn-outline-danger" onClick={onReject}>Reject</button>
                                    <button type="button" className="btn btn-maverick" onClick={onApprove} disabled={viewReq.requestType === 'OPEN' && customer?.kycStatus !== 'DONE'}>Approve</button>
                                </>
                            )}
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default AccountRequests
