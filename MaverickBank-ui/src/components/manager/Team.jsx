import axios from "axios"
import { useEffect, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"

function Team() {

    const [team, setTeam] = useState([])
    const [reload, setReload] = useState(0)

    useEffect(() => {
        const getTeam = async () => {
            try {
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/executive/team`, { 
                    headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
                 })
                setTeam(response.data)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load the team",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        getTeam()
    }, [reload])

    return (
        <div className="card">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <span className="fw-semibold">Executives in my branch</span>
                <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => setReload(reload + 1)}><i className="bi bi-arrow-clockwise me-1"></i>Refresh</button>
            </div>
            <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                        <tr>
                            <th>S.No</th>
                            <th>Name</th>
                            <th>Job title</th>
                            <th>Email</th>
                            <th>Contact</th>
                            <th className="text-end">Open requests</th>
                            <th className="text-end">Open loans</th>
                        </tr>
                    </thead>
                    <tbody>
                        {team.length === 0 && <tr><td colSpan="7" className="text-center text-muted py-4">No executives in the branch</td></tr>}
                        {team.map((t, index) => (
                            <tr key={index}>
                                <td>{index + 1}</td>
                                <td>{t.name}</td>
                                <td>{t.jobTitle ? t.jobTitle : "-"}</td>
                                <td>{t.email}</td>
                                <td>{t.contact}</td>
                                <td className="text-end">{t.pendingRequests}</td>
                                <td className="text-end">{t.pendingLoans}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    )
}

export default Team
