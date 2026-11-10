import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import StatusChart from "../common/StatusChart"
import BarChart from "../common/BarChart"

function AdminDashboard() {

    const memToken = localStorage.getItem('token')

    const [requestStat, setRequestStat] = useState({ labels: [], values: [] })
    const [loanStat, setLoanStat] = useState({ labels: [], values: [] })
    const [accountStat, setAccountStat] = useState({ labels: [], values: [] })
    const [kycStat, setKycStat] = useState({ labels: [], values: [] })
    const [branchStat, setBranchStat] = useState({ labels: [], values: [] })
    const [executiveCount, setExecutiveCount] = useState(0)
    const [suspendedCount, setSuspendedCount] = useState(0)

    useEffect(() => {
        const loadDashboard = async () => {
            try {

                const request = await axios.get(`${API_SERVER_URL_DEV}/api/account-request/stat/status`, {
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setRequestStat(request.data)

                const loan = await axios.get(`${API_SERVER_URL_DEV}/api/loan/stat/status`, {
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setLoanStat(loan.data)

                const acc = await axios.get(`${API_SERVER_URL_DEV}/api/account/stat/type`, {
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setAccountStat(acc.data)

                const kyc = await axios.get(`${API_SERVER_URL_DEV}/api/customer/stat/kyc`, {
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setKycStat(kyc.data)

                const branch = await axios.get(`${API_SERVER_URL_DEV}/api/account/stat/branch`, {
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setBranchStat(branch.data)

                const executive = await axios.get(`${API_SERVER_URL_DEV}/api/executive/filter`, {
                    params: { page: 0, size: 1 },
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setExecutiveCount(executive.data.paginationInfo.totalRecords)

                const suspended = await axios.get(`${API_SERVER_URL_DEV}/api/account/filter`, {
                    params: { status: 'SUSPENDED', page: 0, size: 1 },
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setSuspendedCount(suspended.data.paginationInfo.totalRecords)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load the dashboard",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        loadDashboard()
    }, [])


    const sum = (stat) => stat.values.reduce((total, v) => total + v, 0)

    return (
        <div>
            <div className="row g-3 mb-4">
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">Customers</div><div className="fs-3 fw-semibold">{sum(kycStat)}</div></div></div></div>
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">Executives</div><div className="fs-3 fw-semibold">{executiveCount}</div></div></div></div>
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">Open accounts</div><div className="fs-3 fw-semibold">{sum(accountStat)}</div></div></div></div>
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">Suspended accounts</div><div className="fs-3 fw-semibold">{suspendedCount}</div></div></div></div>
            </div>

            <div className="row g-3 mb-4">
                <div className="col-md-6 col-xl-3"><StatusChart title="Account requests by status" labels={requestStat.labels} values={requestStat.values} /></div>
                <div className="col-md-6 col-xl-3"><StatusChart title="Loans by status" labels={loanStat.labels} values={loanStat.values} /></div>
                <div className="col-md-6 col-xl-3"><StatusChart title="Accounts by type" labels={accountStat.labels} values={accountStat.values} /></div>
                <div className="col-md-6 col-xl-3"><StatusChart title="Customers by KYC" labels={kycStat.labels} values={kycStat.values} /></div>
            </div>

            <div className="row g-3 mb-4">
                <div className="col-12"><BarChart title="Open accounts by branch" label="Accounts" labels={branchStat.labels} values={branchStat.values} /></div>
            </div>


        </div>
    )
}

export default AdminDashboard
