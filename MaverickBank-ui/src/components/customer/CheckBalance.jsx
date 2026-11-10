import axios from "axios"
import { useState } from "react"
import { API_SERVER_URL_DEV } from "../../config";

function CheckBalance({ accountList, token }) {

    const [account, setAccount] = useState("")
    const [password, setPassword] = useState("")
    const [balance, setBalance] = useState(null)

    const onCheck = async (e) => {
        e.preventDefault()
        setBalance(null)

        try {
            const acc = accountList.find(ac => ac.accountNumber == account)
            let obj = { id : acc.id,
                  password : password
                 }
            const res = await axios.post(
                `${API_SERVER_URL_DEV}/api/account/check-balance`,obj,
                { headers: { Authorization: 'Bearer ' + localStorage.getItem('token') } }
            )
            setBalance(res.data.balance)
            setPassword("")
        } catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not fetch balance",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    return (
        <form className="row g-3 mt-3 pt-3 border-top" onSubmit={onCheck}>
            <h6 className="col-12 mb-0">Check balance</h6>
            <div className="col-md-6">
                <label className="form-label">Account</label>
                <select className="form-select" value={account} onChange={(e) => setAccount(e.target.value)} required>
                    <option value="">Select account</option>
                    {
                        accountList.map((ac, index) => (
                            (ac.status === 'ACTIVE' || ac.status === 'CLOSURE_REQUEST') &&
                            <option key={index} value={ac.accountNumber}>{ac.accountType} - {ac.accountNumber}</option>
                        ))
                    }
                </select>
            </div>
            <div className="col-md-6">
                <label className="form-label">Password</label>
                <input type="password" className="form-control" value={password}
                    onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <div className="col-12">
                <button className="btn btn-outline-primary">Show balance</button>
            </div>
            {balance !== null && (
                <div className="col-12">
                    <div className="alert alert-success mb-0">
                        Available balance:{" "}
                        <strong>{Number(balance).toLocaleString("en-IN", { style: "currency", currency: "INR" })}</strong>
                    </div>
                </div>
            )}
        </form>
    )
}

export default CheckBalance