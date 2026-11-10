import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import StatusBadge from "../common/StatusBadge"
import Pagination from "../common/Pagination"
import TransactionList from "../common/TransactionList"

function Accounts({ admin = false }) {

    const [branches, setBranches] = useState([])

    const [accounts, setAccounts] = useState([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalRecords, setTotalRecords] = useState(0)
    const [recordsPerPage, setRecordsPerPage] = useState(0)
    const [page, setPage] = useState(0)
    const size = 7
    const [reload, setReload] = useState(0)

    const [branchName, setBranchName] = useState("")
    const [accountNumber, setAccountNumber] = useState("")
    const [accountType, setAccountType] = useState("")
    const [status, setStatus] = useState("")
    const [nameInput, setNameInput] = useState("")
    const [customerName, setCustomerName] = useState("")


    const [viewAcc, setViewAcc] = useState(null)
    const [transactions, setTransactions] = useState([])


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
        const getAccounts = async () => {
            try {
                const params = { page, size }
                if (branchName) params.branchName = branchName
                if (accountNumber) params.accountNumber = accountNumber
                if (accountType) params.accountType = accountType
                if (status) params.status = status
                if (customerName) params.customerName = customerName

                const response = await axios.get(`${API_SERVER_URL_DEV}/api/account/filter`, {
                    params,
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
                })

                setAccounts(response.data.list)
                setTotalPages(response.data.paginationInfo.totalPages)
                setTotalRecords(response.data.paginationInfo.totalRecords)
                setRecordsPerPage(response.data.paginationInfo.recordsPerPage)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load accounts",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        getAccounts()
    }, [page, size, branchName, accountNumber, accountType, status, customerName, reload])

    const onSearch = (e) => {
        e.preventDefault()
        setCustomerName(nameInput.trim())
        setPage(0)
    }

    const onClear = () => {
        setBranchName("")
        setAccountNumber("")
        setAccountType("")
        setStatus("")
        setNameInput("")
        setCustomerName("")
        setPage(0)
    }

    const onView = async (ac) => {
        setViewAcc(ac)
        setTransactions([])
        try {
            const response = await axios.get(`${API_SERVER_URL_DEV}/api/transaction/get/${ac.id}`, {
                params: { page: 0, size: 20 },
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            setTransactions(response.data)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not load transactions",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }


    const onSuspend = async (ac) => {
        const result = await window.Swal.fire({
            icon: "warning",
            title: "Suspend the account?",
            text: `${ac.name} cannot use account ${ac.accountNumber} until it is activated again`,
            showCancelButton: true,
            confirmButtonText: "Suspend",
            confirmButtonColor: "#dc3545"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/account/suspend/${ac.id}`, null, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            window.Swal.fire({ icon: "success", title: "Account suspended", showConfirmButton: false, timer: 1500 })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not suspend",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onActivate = async (ac) => {
        const result = await window.Swal.fire({
            icon: "question",
            title: "Activate the account?",
            showCancelButton: true,
            confirmButtonText: "Activate"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/account/activate/${ac.id}`, null, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            window.Swal.fire({
                icon: "success",
                title: "Account activated",
                showConfirmButton: false, timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not activate",
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
                        <div className="col-md-3">
                            <label className="form-label">Account number</label>
                            <input type="number" className="form-control" value={accountNumber} onChange={(e) => {
                                setAccountNumber(e.target.value)
                                setPage(0)
                            }} />
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Type</label>
                            <select className="form-select" value={accountType} onChange={(e) => {
                                setAccountType(e.target.value)
                                setPage(0)
                            }}>
                                <option value="">All</option>
                                <option value="SAVINGS">SAVINGS</option>
                                <option value="CHECKING">CHECKING</option>
                                <option value="BUSINESS">BUSINESS</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Status</label>
                            <select className="form-select" value={status} onChange={(e) => {
                                setStatus(e.target.value)
                                setPage(0)
                            }}>
                                <option value="">All</option>
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="CLOSURE_REQUEST">CLOSURE REQUEST</option>
                                <option value="SUSPENDED">SUSPENDED</option>
                                <option value="CLOSED">CLOSED</option>
                            </select>
                        </div>
                        <div className="col-md-4">
                            <label className="form-label">Customer name</label>
                            <div className="input-group">
                                <input type="text" className="form-control" placeholder="Search" value={nameInput} onChange={(e) => setNameInput(e.target.value)} />
                                <button type="submit" className="btn btn-outline-secondary"><i className="bi bi-search"></i></button>
                            </div>
                        </div>
                        <div className="col-md-1 d-grid">
                            <button type="button" className="btn btn-outline-secondary" onClick={onClear}>Clear</button>
                        </div>
                        {admin && (
                            <div className="col-md-3">
                                <label className="form-label">Branch</label>
                                <select className="form-select" value={branchName} onChange={(e) => {
                                    setBranchName(e.target.value)
                                    setPage(0)
                                }}>
                                    <option value="">All branches</option>
                                    {branches.map(b => <option key={b.id} value={b.name}>{b.name}</option>)}
                                </select>
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
                                <th>Customer</th>
                                <th>Account number</th>
                                <th>Type</th>
                                <th>Branch</th>
                                <th className="text-end">Balance</th>
                                <th>Opened on</th>
                                <th>Executive</th>
                                <th>Status</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {accounts.length === 0 && <tr><td colSpan="10" className="text-center text-muted py-4">No accounts found</td></tr>}
                            {accounts.map((ac, index) => (
                                <tr key={index}>
                                    <td>{page * size + index + 1}</td>
                                    <td>{ac.name}</td>
                                    <td>{ac.accountNumber}</td>
                                    <td>{ac.accountType}</td>
                                    <td>{ac.branchName}</td>
                                    <td className="text-end">₹ {ac.balance}</td>
                                    <td>{ac.openedOn}</td>
                                    <td>{ac.executiveName ?? "-"}</td>
                                    <td><StatusBadge status={ac.status} /></td>
                                    <td className="text-end text-nowrap">
                                        {admin && ac.status === 'ACTIVE' && <button type="button" className="btn btn-sm btn-outline-danger me-2" onClick={() => onSuspend(ac)}>Suspend</button>}
                                        {admin && ac.status === 'SUSPENDED' && <button type="button" className="btn btn-sm btn-outline-success me-2" onClick={() => onActivate(ac)}>Activate</button>}
                                        
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

            <div className="modal fade" id="accModal" tabIndex="-1" aria-hidden="true">
                <div className="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">{viewAcc ? `${viewAcc.name}, ${viewAcc.accountType} ${viewAcc.accountNumber}` : "Account"}</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
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

export default Accounts
