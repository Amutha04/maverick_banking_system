import axios from "axios"
import { useEffect, useState } from "react"
import { Link } from "react-router"
import { API_SERVER_URL_DEV } from "../../config"
import StatusChart from "../common/StatusChart"

function ExecutiveDashboard() {

    const memToken = localStorage.getItem('token')

    const [requestStat, setRequestStat] = useState({ labels: [], values: [] })
    const [loanStat, setLoanStat] = useState({ labels: [], values: [] })
    const [accountStat, setAccountStat] = useState({ labels: [], values: [] })
    const [kycStat, setKycStat] = useState({ labels: [], values: [] })


    const [myRequestCount, setMyRequestCount] = useState(0)
    const [myLoanCount, setMyLoanCount] = useState(0)
    const [toDisburseCount, setToDisburseCount] = useState(0)

    useEffect(() => {
        const load = async () => {
            try {
   
                const req = await axios.get(`${API_SERVER_URL_DEV}/api/account-request/stat/status`, { 
                    headers : { Authorization: 'Bearer ' + memToken }
                 })
                setRequestStat(req.data)

                const loan = await axios.get(`${API_SERVER_URL_DEV}/api/loan/stat/status`, { 
                    headers : { Authorization: 'Bearer ' + memToken }
                 })
                setLoanStat(loan.data)

                const acc = await axios.get(`${API_SERVER_URL_DEV}/api/account/stat/type`, { 
                    headers : { Authorization: 'Bearer ' + memToken }
                 })
                setAccountStat(acc.data)

                const kyc = await axios.get(`${API_SERVER_URL_DEV}/api/customer/stat/kyc`, { 
                    headers : { Authorization: 'Bearer ' + memToken }
                 })
                setKycStat(kyc.data)

                const myReq = await axios.get(`${API_SERVER_URL_DEV}/api/account-request/filter`, { params: { requestStatus: 'IN_PROCESS', page: 0, size: 5 }, 
                headers : { Authorization: 'Bearer ' + memToken } })
            
                setMyRequestCount(myReq.data.paginationInfo.totalRecords)

                const myLoan = await axios.get(`${API_SERVER_URL_DEV}/api/loan/filter`, { params: { loanStatus: 'IN_PROCESS', page: 0, size: 5 }, 
                headers : { Authorization: 'Bearer ' + memToken } })
            
                setMyLoanCount(myLoan.data.paginationInfo.totalRecords)

                const disburseCount = await axios.get(`${API_SERVER_URL_DEV}/api/loan/filter`, { params: { loanStatus: 'VERIFIED', page: 0, size: 1 }, 
                headers : { Authorization: 'Bearer ' + memToken } })
                setToDisburseCount(disburseCount.data.paginationInfo.totalRecords)
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
        load()
    }, [])

    const kycPending = kycStat.values[kycStat.labels.indexOf('PENDING')] ?? 0

    return (
        <div>
            <div className="row g-3 mb-5">
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">Requests waiting for me</div><div className="fs-3 fw-semibold">{myRequestCount}</div></div></div></div>
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">Loans to sanction</div><div className="fs-3 fw-semibold">{myLoanCount}</div></div></div></div>
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">Loans to disburse</div><div className="fs-3 fw-semibold">{toDisburseCount}</div></div></div></div>
                <div className="col-md-6 col-xl-3"><div className="card h-100"><div className="card-body"><div className="stat-label">KYC pending in branch</div><div className="fs-3 fw-semibold">{kycPending}</div></div></div></div>
            </div>

            <div className="row g-3 mb-4">
                <div className="col-md-6 col-xl-3"><StatusChart title="Account requests by status" labels={requestStat.labels} values={requestStat.values} /></div>
                <div className="col-md-6 col-xl-3"><StatusChart title="Loans by status" labels={loanStat.labels} values={loanStat.values} /></div>
                <div className="col-md-6 col-xl-3"><StatusChart title="Accounts by type" labels={accountStat.labels} values={accountStat.values} /></div>
                <div className="col-md-6 col-xl-3"><StatusChart title="Customers by KYC" labels={kycStat.labels} values={kycStat.values} /></div>
            </div>
        </div>
    )
}

export default ExecutiveDashboard
