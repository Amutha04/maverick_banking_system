import axios from "axios"
import { useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"

function ChangePassword() {

    const [currentPassword, setCurrentPassword] = useState("")
    const [newPassword, setNewPassword] = useState("")
    const [confirmPassword, setConfirmPassword] = useState("")
    const [errMsg, setErrMsg] = useState("")

    const onSubmit = async (e) => {
        e.preventDefault()
        setErrMsg("")

        if (newPassword !== confirmPassword) {
            setErrMsg("New passwords do not match")
            return
        }
        if (newPassword === currentPassword) {
            setErrMsg("New password must be different from the current one")
            return
        }

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/auth/change-password`, { currentPassword, newPassword }, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            setCurrentPassword("")
            setNewPassword("")
            setConfirmPassword("")
            window.Swal.fire({
                icon: "success",
                title: "Password changed",
                text: "Use the new password from the next login",
                showConfirmButton: false,
                timer: 2000
            })
        }
        catch (err) {
            // validation errors come as a map of field -> message, the other errors as message
            const data = err?.response?.data
            setErrMsg(data?.message || (data ? Object.values(data).join(", ") : "Something went wrong"))
        }
    }

    return (
        <div className="card">
            <div className="card-header bg-white fw-semibold">Change password</div>
            <div className="card-body">
                {errMsg && <div className="alert alert-danger py-2 small">{errMsg}</div>}
                <form onSubmit={onSubmit}>
                    <div className="mb-3">
                        <label className="form-label">Current password</label>
                        <input type="password" className="form-control" required value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} />
                    </div>
                    <div className="mb-3">
                        <label className="form-label">New password</label>
                        <input type="password" className="form-control" required minLength={6} value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Confirm new password</label>
                        <input type="password" className="form-control" required value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
                    </div>
                    <button type="submit" className="btn btn-maverick">Change password</button>
                </form>
            </div>
        </div>
    )
}

export default ChangePassword
