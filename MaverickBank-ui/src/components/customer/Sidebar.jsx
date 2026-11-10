import { Link, NavLink, useNavigate } from "react-router"
import { useSelector } from "react-redux"

function Sidebar() {
    const navigate = useNavigate()
    const memToken = localStorage.getItem('token')

    
    const loanList = useSelector(state => state.customerLoans)
    const offers = loanList.filter(l => l.loanStatus === 'SANCTIONED').length

    const onLogout = () => {
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
                    <NavLink className="nav-link" end to={`/customer/${memToken}`}><i className="bi bi-speedometer2"></i>Dashboard</NavLink>
                    <NavLink className="nav-link" to={`/customer/${memToken}/my-accounts`}><i className="bi bi-wallet2"></i>My Accounts</NavLink>
                    <NavLink className="nav-link" to={`/customer/${memToken}/transaction`}><i className="bi bi-arrow-left-right"></i>Transactions</NavLink>
                    <NavLink className="nav-link" to={`/customer/${memToken}/transaction-history`}><i className="bi bi-clock-history"></i>Transaction History</NavLink>
                    <NavLink className="nav-link" to={`/customer/${memToken}/beneficiaries`}><i className="bi bi-people"></i>Beneficiaries</NavLink>
                    <NavLink className="nav-link" to={`/customer/${memToken}/loans`}><i className="bi bi-cash-coin"></i>Loans{offers > 0 && <span className="badge rounded-pill bg-danger ms-2">{offers}</span>}</NavLink>
                    <NavLink className="nav-link" to={`/customer/${memToken}/profile`}><i className="bi bi-person-badge"></i>My Profile</NavLink>
                    
                        <Link className="nav-link logout" onClick={() => { onLogout() }}>
                            <i className="bi bi-box-arrow-right me-2"></i>
                            Logout
                        </Link>
                  
                </nav>
            </div>
        </aside>
    )
}

export default Sidebar