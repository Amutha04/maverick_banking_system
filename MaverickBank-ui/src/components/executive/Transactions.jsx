import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import TransactionList from "../common/TransactionList"
import Pagination from "../common/Pagination"

function Transactions({ admin = false }) {

    const [branches, setBranches] = useState([])

    const [transactions, setTransactions] = useState([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalRecords, setTotalRecords] = useState(0)
    const [recordsPerPage, setRecordsPerPage] = useState(0)
    const [page, setPage] = useState(0)
    const size =7

    const [branchName, setBranchName] = useState("")
    const [accountNumber, setAccountNumber] = useState("")
    const [transactionType, setTransactionType] = useState("")
    const [transactionStatus, setTransactionStatus] = useState("")
    const [from, setFrom] = useState("")
    const [to, setTo] = useState("")

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
        const getTransactions = async () => {
            try {
                const params = { page, size }
                if (branchName) params.branchName = branchName
                if (accountNumber) params.accountNumber = accountNumber
                if (transactionType) params.transactionType = transactionType
                if (transactionStatus) params.transactionStatus = transactionStatus
                if (from) params.from = from
                if (to) params.to = to

                const response = await axios.get(`${API_SERVER_URL_DEV}/api/transaction/filter`, { params, 
                    headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
                 })

                setTransactions(response.data.list)
                setTotalPages(response.data.paginationInfo.totalPages)
                setTotalRecords(response.data.paginationInfo.totalRecords)
                setRecordsPerPage(response.data.paginationInfo.recordsPerPage)
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
        getTransactions()
    }, [page, size, branchName, accountNumber, transactionType, transactionStatus, from, to])


    const onClear = () => {
        setBranchName("")
        setAccountNumber("")
        setTransactionType("")
        setTransactionStatus("")
        setFrom("")
        setTo("")
        setPage(0)
    }

    return (
        <div>
            <div className="card mb-4">
                <div className="card-body">
                    <div className="row g-3 align-items-end">
                        <div className="col-md-3">
                            <label className="form-label">Account number</label>
                            <input type="number" className="form-control" value={accountNumber} onChange={(e)=>{
                                setAccountNumber(e.target.value)
                                setPage(0)
                            }} />
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Type</label>
                            <select className="form-select" value={transactionType} onChange={(e)=>{
                                setTransactionType(e.target.value)
                                setPage(0)
                            }}>
                                <option value="">All types</option>
                                <option value="DEPOSIT">DEPOSIT</option>
                                <option value="WITHDRAW">WITHDRAW</option>
                                <option value="CREDIT">CREDIT</option>
                                <option value="DEBIT">DEBIT</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Status</label>
                            <select className="form-select" value={transactionStatus} onChange={(e)=>{
                                setTransactionStatus(e.target.value)
                                setPage(0)
                            }}>
                                <option value="">All</option>
                                <option value="SUCCESS">SUCCESS</option>
                                <option value="FAILED">FAILED</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">From</label>
                            <input type="date" className="form-control" value={from} onChange={(e)=>{
                                setFrom(e.target.value)
                                setPage(0)
                            }} />
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">To</label>
                            <input type="date" className="form-control" value={to}  onChange={(e)=>{
                                setTo(e.target.value)
                                setPage(0)
                            }}/>
                        </div>
                        <div className="col-md-1 d-grid">
                            <button type="button" className="btn btn-outline-secondary" onClick={onClear}>Clear</button>
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
                    </div>
                </div>
            </div>

            <TransactionList transactions={transactions} isDashboard="false" />

            <div className="d-flex flex-wrap justify-content-center align-items-center gap-4 mt-3">
                
                <Pagination page={page} totalPages={totalPages} totalRecords={totalRecords} recordsPerPage={recordsPerPage} onPageChange={setPage} />
            </div>
        </div>
    )
}

export default Transactions
