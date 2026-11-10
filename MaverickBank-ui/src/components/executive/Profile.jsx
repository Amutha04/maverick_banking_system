import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import ChangePassword from "../common/ChangePassword"

function Profile() {

    const [profile, setProfile] = useState(null)

    useEffect(() => {
        const getProfile = async () => {
            try {
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/executive`, {
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
                })
                setProfile(response.data)
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
        getProfile()
    }, [])

    if (!profile)
        return <span className="text-muted">Loading...</span>

    return (
        <div style={{ maxWidth: "640px" }}>
            <div className="card mb-4">
                <div className="card-header bg-white fw-semibold">My profile</div>
                <div className="card-body">
                    <dl className="row mb-0">
                        <dt className="col-4">Name</dt><dd className="col-8">{profile.name}</dd>
                        <dt className="col-4">Username</dt><dd className="col-8">{profile.username}</dd>
                        <dt className="col-4">Job title</dt><dd className="col-8">{profile.jobTitle ? profile.jobTitle.replace(/_/g, ' ') : "-"}</dd>
                        <dt className="col-4">Branch</dt><dd className="col-8">{profile.branchName.replace(/_/g, ' ')}</dd>
                        <dt className="col-4">Email</dt><dd className="col-8">{profile.email}</dd>
                        <dt className="col-4">Contact</dt><dd className="col-8">{profile.contact}</dd>
                    </dl>
                </div>
            </div>
            <ChangePassword />
        </div>
    )
}

export default Profile
