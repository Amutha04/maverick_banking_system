import "../../styles/customerDashboard.css"
import axios from "axios"
import { useEffect } from "react"
import {  Outlet, useLocation, useNavigate, useParams } from "react-router"
import { API_SERVER_URL_DEV } from "../../config"
import Sidebar from "./Sidebar"
import NavBar from "../common/NavBar"
import { useDispatch } from "react-redux"
import { getAccounts, getBeneficiaries, getBranch, getLoanProducts, getLoans, getTransactions } from "../../store/actions/CustomerActions"


function Home() {

    const params = useParams()
    const navigate = useNavigate()
    const userDetailUrl = API_SERVER_URL_DEV + '/api/auth/user-details'
    const dispatch = useDispatch()
    const location = useLocation()

    
    const sectionTitles = {
        "": "Dashboard",
        "my-accounts": "My Accounts",
        "transaction": "Transactions",
        "transaction-history": "Transaction History",
        "beneficiaries": "Beneficiaries",
        "loans": "Loans",
        "profile": "My Profile"
    }
    const section = location.pathname.split('/').filter(Boolean)[2] ?? ""
    const title = sectionTitles[section] ?? "Dashboard"

    useEffect(() => {
        const urlToken = params.token
        const memToken = localStorage.getItem('token')

        if (urlToken !== memToken)
            navigate("/no-access")

        console.log(`tokens match: ${urlToken === memToken}`)

        const getUserDetails = async () => {
            try {
            
                const resp = await axios.get(userDetailUrl, {
                    headers: { Authorization: 'Bearer ' + memToken }
                })

                if (resp.data.role !== 'CUSTOMER') {
                    navigate("/no-access")
                }
            }

            catch (err) {
                navigate("/no-access")
            }
        }
        getUserDetails()

        dispatch(getAccounts())

        dispatch(getLoans())

        dispatch(getTransactions())

        dispatch(getBranch())

        dispatch(getBeneficiaries())

        dispatch(getLoanProducts())

    }, [params.token])

    

    return (
        <div>
            <Sidebar/>
            <div className="main">
                <NavBar title={title} username = {localStorage.getItem('username')}/>
                <div className="container-fluid p-3 p-md-4">
                    <Outlet/>
                </div>
            </div>
        </div>
    )
}

export default Home