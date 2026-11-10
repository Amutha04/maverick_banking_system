import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import StatusBadge from "../common/StatusBadge"
import KycDocument from "../common/KycDocument"
import ChangePassword from "../common/ChangePassword"

function Profile() {

    const [profile, setProfile] = useState(null)

    useEffect(() => {
        const getProfile = async () => {
            try {
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/customer`, {
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

    const uploaded = profile.aadhaarUrl || profile.panUrl || profile.photoUrl || profile.signatureUrl

    return (
        <div className="row g-4">
            <div className="col-lg-6">
                <div className="card h-100">
                    <div className="card-header bg-white fw-semibold">My profile</div>
                    <div className="card-body">
                        <dl className="row mb-0">
                            <dt className="col-4">Name</dt><dd className="col-8">{profile.name}</dd>
                            <dt className="col-4">Username</dt><dd className="col-8">{profile.username}</dd>
                            <dt className="col-4">Date of birth</dt><dd className="col-8">{profile.dob}</dd>
                            <dt className="col-4">Gender</dt><dd className="col-8">{profile.gender ?? "-"}</dd>
                            <dt className="col-4">Contact</dt><dd className="col-8">{profile.contact}</dd>
                            <dt className="col-4">Email</dt><dd className="col-8">{profile.email ?? "-"}</dd>
                            <dt className="col-4">Address</dt><dd className="col-8">{profile.address ?? "-"}</dd>
                            <dt className="col-4">Aadhaar</dt><dd className="col-8">{profile.aadhaarNumber ?? "-"}</dd>
                            <dt className="col-4">PAN</dt><dd className="col-8">{profile.panNumber ?? "-"}</dd>
                            <dt className="col-4">KYC</dt><dd className="col-8"><StatusBadge status={profile.kycStatus} /></dd>
                        </dl>
                    </div>
                </div>
            </div>
            <div className="col-lg-6">
                <div className="card h-100">
                    <div className="card-header bg-white fw-semibold">KYC documents</div>
                    <div className="card-body">
                        {!uploaded && <p className="text-muted small">The documents are uploaded while opening the first account.</p>}
                        {profile.kycStatus === 'PENDING' && uploaded && <p className="text-muted small">Uploaded, waiting for the bank to verify them.</p>}
                        <div className="row g-3">
                            <KycDocument label="Aadhaar" file={profile.aadhaarUrl} />
                            <KycDocument label="PAN" file={profile.panUrl} />
                            <KycDocument label="Photo" file={profile.photoUrl} />
                            <KycDocument label="Signature" file={profile.signatureUrl} />
                        </div>
                    </div>
                </div>
            </div>
            <div className="col-lg-6">
                <ChangePassword />
            </div>
        </div>
    )
}

export default Profile
