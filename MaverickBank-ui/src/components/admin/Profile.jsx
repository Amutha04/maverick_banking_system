import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import ChangePassword from "../common/ChangePassword"

function Profile() {

    const [details, setDetails] = useState(null)

    useEffect(() => {
        const getDetails = async () => {
            try {
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/auth/user-details`, {
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
                })
                setDetails(response.data)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load the profile",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        getDetails()
    }, [])

    if (!details)
        return <span className="text-muted">Loading...</span>

    return (
        <div >
            <div className="card mb-4">
                <div className="card-header bg-white fw-semibold">My profile</div>
                <div className="card-body">
                    <dl className="row mb-0">
                        <dt className="col-4">Username</dt><dd className="col-8">{details.username}</dd>
                        <dt className="col-4">Role</dt><dd className="col-8">{details.role}</dd>
                        <dt className="col-4">Status</dt><dd className="col-8">{details.isActive ? "Active" : "Inactive"}</dd>
                        <dt className="col-4">Member since</dt><dd className="col-8">{details.createdAt ?? "-"}</dd>
                    </dl>
                </div>
            </div>
            <ChangePassword />
        </div>
    )
}

export default Profile
