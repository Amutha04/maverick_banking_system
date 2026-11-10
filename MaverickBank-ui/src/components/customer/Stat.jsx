import { useSelector } from "react-redux"

function Stat() {
    const accountList = useSelector(state => state.customerAccounts)
    const loanList = useSelector(state => state.customerLoans)

    const totalBalance = accountList.reduce((sum, account) => sum + account.balance, 0)
    const activeLoanCount = [...loanList.filter(l => l.loanStatus === 'OPEN')].length
    const activeAccountCount = [...accountList.filter(ac => ac.status === 'ACTIVE')].length
    return (
        <div className="row g-3 mb-4">
            <div className="col-md-4"><div className="card h-100"><div className="card-body"><div className="stat-label">Total balance</div><div className="fs-3 fw-semibold">{totalBalance}</div></div></div></div>
            <div className="col-md-4"><div className="card h-100"><div className="card-body"><div className="stat-label">Active Accounts</div><div className="fs-3 fw-semibold">{activeAccountCount}</div></div></div></div>
    <div className="col-md-4"><div className="card h-100"><div className="card-body"><div className="stat-label">Active loans</div><div className="fs-3 fw-semibold">{activeLoanCount}</div></div></div></div>
        </div>
    )
}

export default Stat