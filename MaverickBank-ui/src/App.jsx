import { Route, Routes } from "react-router"
import CustomerDashboard from "./components/customer/CustomerDashboard"
import Login from "./components/auth/Login"
import AdminHome from "./components/admin/AdminHome"
import AdminDashboard from "./components/admin/AdminDashboard"
import Customers from "./components/admin/Customers"
import Executives from "./components/admin/Executives"
import AdminProfile from "./components/admin/Profile"
import CustomerProfile from "./components/customer/Profile"
import ExecutiveHome from "./components/executive/ExecutiveHome"
import ExecutiveDashboard from "./components/executive/ExecutiveDashboard"
import AccountRequests from "./components/executive/AccountRequests"
import ExecutiveLoans from "./components/executive/Loans"
import ExecutiveAccounts from "./components/executive/Accounts"
import ExecutiveTransactions from "./components/executive/Transactions"
import Profile from "./components/executive/Profile"
import ManagerHome from "./components/manager/ManagerHome"
import ManagerDashboard from "./components/manager/ManagerDashboard"
import Team from "./components/manager/Team"
import SignUp from "./components/customer/SignUp"
import PageNotFound from "./components/PageNotFound"
import MyAccounts from "./components/customer/MyAccounts"
import Transactions from "./components/customer/Transactions"
import TransactionHistory from "./components/customer/TransactionHistory"
import Beneficiaries from "./components/customer/Beneficiaries"
import Loans from "./components/customer/Loans"
import Home from "./components/customer/Home"

function App() {
  return (
    <div>
      <Routes>
        <Route path="" element={<Login />} />
        <Route path="customer/signup" element={<SignUp />} />
        <Route path="customer/:token" element={<Home />}>
          <Route path="" element={<CustomerDashboard/>}/>
          <Route path="my-accounts" element={<MyAccounts />} />
          <Route path="transaction" element={<Transactions />} />
          <Route path="transaction-history" element={<TransactionHistory />} />
          <Route path="beneficiaries" element={<Beneficiaries />} />
          <Route path="loans" element={<Loans />} />
          <Route path="profile" element={<CustomerProfile />} />
        </Route>
        <Route path="admin/:token" element={<AdminHome />}>
          <Route path="" element={<AdminDashboard />} />
          <Route path="customers" element={<Customers />} />
          <Route path="executives" element={<Executives />} />
          <Route path="account-requests" element={<AccountRequests admin />} />
          <Route path="loans" element={<ExecutiveLoans admin />} />
          <Route path="accounts" element={<ExecutiveAccounts admin />} />
          <Route path="transactions" element={<ExecutiveTransactions admin />} />
          <Route path="profile" element={<AdminProfile />} />
        </Route>
        <Route path="executive/:token" element={<ExecutiveHome />}>
          <Route path="" element={<ExecutiveDashboard />} />
          <Route path="account-requests" element={<AccountRequests />} />
          <Route path="loans" element={<ExecutiveLoans />} />
          <Route path="accounts" element={<ExecutiveAccounts />} />
          <Route path="transactions" element={<ExecutiveTransactions />} />
          <Route path="profile" element={<Profile />} />
        </Route>
        <Route path="manager/:token" element={<ManagerHome />}>
          <Route path="" element={<ManagerDashboard />} />
          <Route path="account-requests" element={<AccountRequests manager />} />
          <Route path="loans" element={<ExecutiveLoans manager />} />
          <Route path="team" element={<Team />} />
          <Route path="accounts" element={<ExecutiveAccounts />} />
          <Route path="transactions" element={<ExecutiveTransactions />} />
          <Route path="profile" element={<Profile />} />
        </Route>
        <Route path="*" element={<PageNotFound />} />
      </Routes>

    </div>

  )
}

export default App
