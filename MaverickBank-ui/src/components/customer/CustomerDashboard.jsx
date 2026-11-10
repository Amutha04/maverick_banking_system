import "../../styles/customerDashboard.css"
import { useSelector } from "react-redux"
import AccountList from "./AccountList"
import Stat from "./Stat"
import TransactionList from "../common/TransactionList"

function CustomerDashboard() {
   
    const transactionList = useSelector(state => state.customerTransactions)

    return (
        <div>
            <Stat/>
            <AccountList/>
            
            <TransactionList transactions={transactionList.slice(0, 7)} isDashboard="true"/>
        </div>
    )
}

export default CustomerDashboard
