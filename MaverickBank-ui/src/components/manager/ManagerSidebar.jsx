import { NavLink, useNavigate } from "react-router"

function ManagerSidebar() {
    const navigate = useNavigate()
    const memToken = localStorage.getItem('token')

    const onLogout = (e) => {
        e.preventDefault()
        localStorage.clear()
        navigate("/")
    }

    return (
        <aside className="offcanvas-md offcanvas-start sidebar" tabIndex="-1" id="sidebar">
            <div className="sb-bg d-flex flex-column w-100">
                <div className="p-3 border-bottom border-secondary d-flex justify-content-between align-items-center">
                    <span className="text-white fs-5 fw-semibold"><i className="bi bi-bank2 me-2"></i>Maverick Bank</span>
                    <button type="button" className="btn-close btn-close-white d-md-none" data-bs-dismiss="offcanvas" data-bs-target="#sidebar"></button>
                </div>
                <nav className="nav flex-column p-2">
                    <NavLink className="nav-link" end to={`/manager/${memToken}`}><i className="bi bi-speedometer2"></i>Dashboard</NavLink>
                    <NavLink className="nav-link" to={`/manager/${memToken}/account-requests`}><i className="bi bi-inbox"></i>Account Requests</NavLink>
                    <NavLink className="nav-link" to={`/manager/${memToken}/loans`}><i className="bi bi-cash-coin"></i>Loans</NavLink>
                    <NavLink className="nav-link" to={`/manager/${memToken}/team`}><i className="bi bi-people"></i>My Team</NavLink>
                    <NavLink className="nav-link" to={`/manager/${memToken}/accounts`}><i className="bi bi-wallet2"></i>Accounts</NavLink>
                    <NavLink className="nav-link" to={`/manager/${memToken}/transactions`}><i className="bi bi-arrow-left-right"></i>Transactions</NavLink>
                    <NavLink className="nav-link" to={`/manager/${memToken}/profile`}><i className="bi bi-person-badge"></i>My Profile</NavLink>
                    <a href="#" className="nav-link logout" onClick={onLogout}>
                        <i className="bi bi-box-arrow-right me-2"></i>
                        Logout
                    </a>
                </nav>
            </div>
        </aside>
    )
}

export default ManagerSidebar
