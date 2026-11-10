import { Link } from "react-router"
import StatusBadge from "./StatusBadge"

function TransactionList({ transactions = [], isDashboard }) {

    const memToken = localStorage.getItem('token')

    const isCredit = (tnx) => tnx.transactionType === 'CREDIT' || tnx.transactionType === 'DEPOSIT'

    const amountClass = (tnx) => {
        if (tnx.transactionStatus === 'FAILED')
            return 'text-muted'
        return isCredit(tnx) ? 'text-success' : 'text-danger'
    }

    return (
        <div className="card">
            {(isDashboard === "true" && <div className="card-header bg-white d-flex justify-content-between">
                <span className="fw-semibold">Recent transactions</span>
                <Link to={`/customer/${memToken}/transaction-history`}>View all</Link>
            </div>)}
            <div className="table-responsive">
                <table className="table table-hover mb-0">
                    <thead className="table-light">
                        <tr>
                            <th>Date</th>
                            <th>ID</th>
                            <th>Description</th>
                            <th>Type</th>
                            <th>From</th>
                            <th>To</th>
                            <th className="text-end">Amount</th>
                            <th className="text-end">Balance</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            transactions.length === 0 && (
                                <tr><td colSpan="9" className="text-center text-muted py-4">No transactions found</td></tr>
                            )
                        }
                        {
                            transactions.map((tnx, index) => (
                                <tr key={index}>
                                    <td>{tnx.timeStamp}</td>
                                    <td>{tnx.referenceNumber}</td>
                                    <td>{tnx.remarks ? tnx.remarks : "-"}</td>
                                    <td>{tnx.transactionType}</td>
                                    <td>{tnx.accountNumber === null ? "-" : tnx.accountNumber}</td>
                                    <td>{tnx.destinationAccount === null ? "-" : tnx.destinationAccount}</td>
                                    <td className={`text-end fw-semibold ${amountClass(tnx)}`}>{isCredit(tnx) ? '+' : '-'} ₹ {tnx.amount}</td>
                                    <td className="text-end">₹ {tnx.balance}</td>
                                    <td><StatusBadge status={tnx.transactionStatus} /></td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
            </div>
        </div>
    )
}

export default TransactionList
