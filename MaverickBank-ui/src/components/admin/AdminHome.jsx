import "../../styles/customerDashboard.css"
import axios from "axios"
import { useEffect } from "react"
import { Outlet, useLocation, useNavigate, useParams } from "react-router"
import { API_SERVER_URL_DEV } from "../../config"
import AdminSidebar from "./AdminSidebar"
import NavBar from "../common/NavBar"

function AdminHome() {

    const params = useParams()
    const navigate = useNavigate()
    const location = useLocation()

    useEffect(() => {
        const urlToken = params.token
        const memToken = localStorage.getItem('token')

        if (urlToken !== memToken) {
            navigate("/no-access")
            return
        }

        const getUserDetails = async () => {
            try {
            
                const resp = await axios.get(API_SERVER_URL_DEV + '/api/auth/user-details', {
                    headers: { Authorization: 'Bearer ' + memToken }
                })

                if (resp.data.role !== 'ADMIN') {
                    navigate("/no-access")
                    return
                }
            }
            catch (err) {
                navigate("/no-access")
            }
        }
        getUserDetails()
    }, [params.token])

    
    const sectionTitles = {
        "": "Bank Dashboard",
        "customers": "Customers",
        "executives": "Executives",
        "account-requests": "Account Requests",
        "loans": "Loans",
        "accounts": "Accounts",
        "transactions": "Transactions",
        "profile": "My Profile"
    }
    const section = location.pathname.split('/').filter(Boolean)[2] ?? ""
    const title = sectionTitles[section] ?? "Bank Dashboard"

    const username = `${localStorage.getItem('username')} (Admin)`

    return (
        <div>
            <AdminSidebar />
            <div className="main">
                <NavBar title={title} username={username} />
                <div className="container-fluid p-3 p-md-4">
                    <Outlet />
                </div>
            </div>
        </div>
    )
}

export default AdminHome
