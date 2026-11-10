import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import StatusBadge from "../common/StatusBadge"
import Pagination from "../common/Pagination"

function AccountRequestList({ reload }) {

    const [requests, setRequests] = useState([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalRecords, setTotalRecords] = useState(0)
    const [recordsPerPage, setRecordsPerPage] = useState(0)

    const [page, setPage] = useState(0)
    const size = 7

    const [requestType, setRequestType] = useState("")
    const [requestStatus, setRequestStatus] = useState("")
    const [requestedFrom, setRequestedFrom] = useState("")
    const [requestedTo, setRequestedTo] = useState("")

    const [viewReq, setViewReq] = useState(null)

    useEffect(() => {
        const getRequests = async () => {
            try {
                const params = { page, size }
                if (requestType) params.requestType = requestType
                if (requestStatus) params.requestStatus = requestStatus
                if (requestedFrom) params.requestedFrom = requestedFrom
                if (requestedTo) params.requestedTo = requestedTo

                const response = await axios.get(`${API_SERVER_URL_DEV}/api/account-request/get`, {
                    params,
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
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
    }, [page, size, requestType, requestStatus, requestedFrom, requestedTo, reload])


    const onFilterChange = (setter) => (e) => {
        setter(e.target.value)
        setPage(0)
    }

    const onClear = () => {
        setRequestType("")
        setRequestStatus("")
        setRequestedFrom("")
        setRequestedTo("")
        setPage(0)
    }

    return (
        <div className="card mb-4">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <div className="fw-semibold">All Requests</div>
            </div>

            <div className="card-body border-bottom">
                <div className="row g-3 align-items-end">
                    <div className="col-md-2">
                        <label className="form-label">Request type</label>
                        <select className="form-select" value={requestType} onChange={onFilterChange(setRequestType)}>
                            <option value="">All</option>
                            <option value="OPEN">OPEN</option>
                            <option value="CLOSE">CLOSE</option>
                        </select>
                    </div>
                    <div className="col-md-3">
                        <label className="form-label">Status</label>
                        <select className="form-select" value={requestStatus} onChange={onFilterChange(setRequestStatus)}>
                            <option value="">All</option>
                            <option value="IN_PROCESS">IN PROCESS</option>
                            <option value="APPROVED">APPROVED</option>
                            <option value="REJECTED">REJECTED</option>
                        </select>
                    </div>
                    <div className="col-md-3">
                        <label className="form-label">Requested from</label>
                        <input type="date" className="form-control" value={requestedFrom} onChange={onFilterChange(setRequestedFrom)} />
                    </div>
                    <div className="col-md-3">
                        <label className="form-label">Requested to</label>
                        <input type="date" className="form-control" value={requestedTo} onChange={onFilterChange(setRequestedTo)} />
                    </div>
                    <div className="col-md-1 d-grid">
                        <button type="button" className="btn btn-outline-secondary" onClick={onClear}>Clear</button>
                    </div>
                </div>
            </div>

            <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                        <tr>
                            <th>S.No</th>
                            <th>ID</th>
                            <th>Request type</th>
                            <th>Account type</th>
                            <th>Executive</th>
                            <th>Requested on</th>
                            <th>Status</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            requests.length === 0 && (
                                <tr><td colSpan="8" className="text-center text-muted py-4">No requests found</td></tr>
                            )
                        }
                        {requests.map((req, index) => (
                            <tr key={index}>
                                <td>{page * size + index + 1}</td>
                                <td>#{req.id}</td>
                                <td>{req.requestType}</td>
                                <td>{req.accountType}</td>
                                <td>
                                    {req.executiveName ? (
                                        <>
                                            <div>{req.executiveName}</div>
                                            <small className="text-muted">{req.executiveEmail}</small>
                                        </>
                                    ) : (
                                        <span className="text-muted">Unassigned</span>
                                    )}
                                </td>
                                <td>{req.requestedOn}</td>
                                <td><StatusBadge status={req.requestStatus} /></td>
                                <td className="text-end text-nowrap">
                                    <button
                                        type="button"
                                        className="btn btn-sm btn-outline-primary"
                                        data-bs-toggle="modal"
                                        data-bs-target="#viewReq"
                                        onClick={() => setViewReq(req)}
                                    >
                                        View
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

            <div className="modal fade" id="viewReq" tabIndex="-1" aria-hidden="true">
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Request Details</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
                        </div>

                        <div className="modal-body">
                            {viewReq && (
                                <dl className="row mb-0">
                                    <dt className="col-5">Request ID</dt>
                                    <dd className="col-7">{viewReq.id}</dd>

                                    <dt className="col-5">Request type</dt>
                                    <dd className="col-7">{viewReq.requestType}</dd>

                                    <dt className="col-5">Account type</dt>
                                    <dd className="col-7">{viewReq.accountType}</dd>

                                    <dt className="col-5">Status</dt>
                                    <dd className="col-7"><StatusBadge status={viewReq.requestStatus} /></dd>

                                    <dt className="col-5">Requested on</dt>
                                    <dd className="col-7">{viewReq.requestedOn}</dd>

                                    <dt className="col-5">Processed on</dt>
                                    <dd className="col-7">{viewReq.processedOn === null ? "-" : viewReq.processedOn}</dd>

                                    <dt className="col-5">Initial deposit</dt>
                                    <dd className="col-7">{viewReq.initialDeposit === null ? "-" : viewReq.initialDeposit}</dd>

                                    <dt className="col-5">Reason</dt>
                                    <dd className="col-7">{viewReq.reason === null ? "-" : viewReq.reason}</dd>

                                    <dt className="col-5">Remark</dt>
                                    <dd className="col-7">{viewReq.remark === null ? "-" : viewReq.remark}</dd>

                                    <dt className="col-5">Executive</dt>
                                    <dd className="col-7">{viewReq.executiveName === null ? "Unassigned" : viewReq.executiveName}</dd>

                                    <dt className="col-5">Executive email</dt>
                                    <dd className="col-7">{viewReq.executiveEmail === null ? "-" : viewReq.executiveEmail}</dd>
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

export default AccountRequestList
