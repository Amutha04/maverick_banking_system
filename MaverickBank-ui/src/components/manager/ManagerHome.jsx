import "../../styles/customerDashboard.css"
import axios from "axios"
import { useEffect, useState } from "react"
import { Outlet, useLocation, useNavigate, useParams } from "react-router"
import { API_SERVER_URL_DEV } from "../../config"
import ManagerSidebar from "./ManagerSidebar"
import NavBar from "../common/NavBar"

function ManagerHome() {

    const params = useParams()
    const navigate = useNavigate()
    const location = useLocation()

    const [profile, setProfile] = useState(null)

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

                if (resp.data.role !== 'EXECUTIVE') {
                    navigate("/no-access")
                    return
                }

                
                if (resp.data.jobTitle !== 'BANK_MANAGER') {
                    navigate(`/executive/${memToken}`)
                    return
                }

                const prof = await axios.get(API_SERVER_URL_DEV + '/api/executive', {
                    headers: { Authorization: 'Bearer ' + memToken }
                })
                setProfile(prof.data)
            }
            catch (err) {
                navigate("/no-access")
            }
        }
        getUserDetails()
    }, [params.token])

    const sectionTitles = {
        "": "Branch Dashboard",
        "account-requests": "Account Requests",
        "loans": "Loans",
        "team": "My Team",
        "accounts": "Accounts",
        "transactions": "Transactions",
        "profile": "My Profile"
    }
    const section = location.pathname.split('/').filter(Boolean)[2] ?? ""
    const title = sectionTitles[section] ?? "Branch Dashboard"

    const username = profile ? `${profile.name}, Manager (${profile.branchName})` : localStorage.getItem('username')

    return (
        <div>
            <ManagerSidebar />
            <div className="main">
                <NavBar title={title} username={username} />
                <div className="container-fluid p-3 p-md-4">
                    <Outlet />
                </div>
            </div>
        </div>
    )
}

export default ManagerHome
