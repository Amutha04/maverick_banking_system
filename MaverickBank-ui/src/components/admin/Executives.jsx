import axios from "axios"
import { useEffect, useRef, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import Pagination from "../common/Pagination"

const jobTitles = ['PERSONAL_BANKER', 'CREDIT_ANALYST', 'LOAN_OFFICER', 'BANK_MANAGER', 'COMPLIANCE_OFFICER']

function Executives() {

    const [branches, setBranches] = useState([])

    const [executives, setExecutives] = useState([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalRecords, setTotalRecords] = useState(0)
    const [recordsPerPage, setRecordsPerPage] = useState(0)
    const [page, setPage] = useState(0)
    const size = 7
    const [reload, setReload] = useState(0)


    const [nameInput, setNameInput] = useState("")
    const [name, setName] = useState("")
    const [jobTitle, setJobTitle] = useState("")
    const [branchName, setBranchName] = useState("")


    const [exName, setExname] = useState("")
    const [email, setEmail] = useState("")
    const [contact, setContact] = useState("")
    const [exJobTitle, setExJobtitle] = useState("")
    const [branchId, setBranchId] = useState("")
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")


    const addCloseRef = useRef(null)


    const [editExec, setEditExec] = useState(null)
    const [newJobTitle, setNewJobTitle] = useState("")
    const jobCloseRef = useRef(null)

    useEffect(() => {
        const getBranches = async () => {
            try {
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/branch/all`)
                setBranches(response.data)
            }
            catch (err) {
                console.log("could not load the branches", err?.response?.data)
            }
        }
        
        const getExecutives = async () => {
            try {
                const params = { page, size }
                const response = await axios.get(`${API_SERVER_URL_DEV}/api/executive/filter`, {
                    params,
                    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
                })

                setExecutives(response.data.list)
                setTotalPages(response.data.paginationInfo.totalPages)
                setTotalRecords(response.data.paginationInfo.totalRecords)
                setRecordsPerPage(response.data.paginationInfo.recordsPerPage)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load executives",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        getBranches()
        getExecutives()

    }, [page, size, name, jobTitle, branchName, reload])

    const onSearch = (e) => {
        e.preventDefault()
        setName(nameInput.trim())
        setPage(0)
    }

    const onAdd = async (e) => {
        e.preventDefault()
        try {
            let obj = {
                name: exName,
                email: email,
                contact: contact,
                jobTitle: exJobTitle || null,
                username: username,
                password: password,
                branchId: branchId
            }
            await axios.post(`${API_SERVER_URL_DEV}/api/executive/add`, obj, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            addCloseRef.current.click()

            window.Swal.fire({
                icon: "success",
                title: "Executive added",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not add",
                text: err?.response?.data || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    const onEditJobTitle = (ex) => {
        setEditExec(ex)
        setNewJobTitle(ex.jobTitle ?? "")
    }

    const onSaveJobTitle = async () => {
        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/executive/update-jobtitle/${editExec.id}`, null, {
                params: { jobTitle: newJobTitle },
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            jobCloseRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Job title updated",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not update",
                text: err?.response?.data || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }


    const onDeactivate = async (ex) => {
        const result = await window.Swal.fire({
            icon: "warning",
            title: "Deactivate the executive?",
            text: `${ex.name} cannot log in any more. The pending work must be finished first.`,
            showCancelButton: true,
            confirmButtonText: "Deactivate",
            confirmButtonColor: "#dc3545"
        })
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/executive/delete/${ex.id}`, null, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })
            window.Swal.fire({
                icon: "success",
                title: "Executive deactivated",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not deactivate",
                text: err?.response?.data || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    return (
        <div>
            <div className="card mb-4">
                <div className="card-body">
                    <form className="row g-3 align-items-end" onSubmit={(e) => onSearch(e)}>
                        <div className="col-md-4">
                            <label className="form-label">Name</label>
                            <div className="input-group">
                                <input type="text" className="form-control" placeholder="Search" value={nameInput} onChange={(e) => setNameInput(e.target.value)} />
                                <button type="submit" className="btn btn-outline-secondary"><i className="bi bi-search"></i></button>
                            </div>
                        </div>
                        <div className="col-md-3">
                            <label className="form-label">Job title</label>
                            <select className="form-select" value={jobTitle} onChange={(e) => setJobTitle(e.target.value)}>
                                <option value="">All</option>
                                {jobTitles.map(j => <option key={j} value={j}>{j}</option>)}
                            </select>
                        </div>
                        <div className="col-md-3">
                            <label className="form-label">Branch</label>
                            <select className="form-select" value={branchName} onChange={() => setBranchName(e.target.value)}>
                                <option value="">All branches</option>
                                {branches.map(b => <option key={b.id} value={b.name}>{b.name}</option>)}
                            </select>
                        </div>
                        <div className="col-md-2 d-grid">
                            <button type="button" className="btn btn-outline-secondary" onClick={() => {
                                setNameInput("")
                                setName("")
                                setJobTitle("")
                                setBranchName("")
                                setPage(0)
                            }}>Clear</button>
                        </div>
                    </form>
                </div>
            </div>

            <div className="card">
                <div className="card-header bg-white d-flex justify-content-between align-items-center">
                    <span className="fw-semibold">Executives</span>
                    <button type="button" className="btn btn-maverick btn-sm" data-bs-toggle="modal" data-bs-target="#addModal" onClick={() => {
                        setExname("")
                        setEmail("")
                        setContact("")
                        setExJobtitle("")
                        setBranchId("")
                        setUsername("")
                        setPassword("")
                    }}><i className="bi bi-person-plus me-1"></i>Add executive</button>
                </div>
                <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-light">
                            <tr>
                                <th>S.No</th>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Job title</th>
                                <th>Branch</th>
                                <th>Email</th>
                                <th>Contact</th>
                                <th>Username</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {executives.length === 0 && <tr><td colSpan="9" className="text-center text-muted py-4">No executives found</td></tr>}
                            {executives.map((ex, index) => (
                                <tr key={index}>
                                    <td>{page * size + index + 1}</td>
                                    <td>#{ex.id}</td>
                                    <td>{ex.name}</td>
                                    <td>{ex.jobTitle ? ex.jobTitle : <span className="text-muted">Not set</span>}</td>
                                    <td>{ex.branchName}</td>
                                    <td>{ex.email}</td>
                                    <td>{ex.contact}</td>
                                    <td>{ex.username}</td>
                                    <td className="text-end text-nowrap">
                                        <button type="button" className="btn btn-sm btn-outline-primary me-2" data-bs-toggle="modal" data-bs-target="#jobModal" onClick={() => onEditJobTitle(ex)}>Job title</button>
                                        <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => onDeactivate(ex)}>Deactivate</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                <div className="card-footer bg-white d-flex flex-wrap justify-content-center align-items-center gap-4">

                    <Pagination page={page} totalPages={totalPages} totalRecords={totalRecords} recordsPerPage={recordsPerPage} onPageChange={setPage} />
                </div>
            </div>

            <div className="modal fade" id="addModal" tabIndex="-1" aria-hidden="true" data-bs-focus="false">
                <div className="modal-dialog modal-lg modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Add executive</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal" ref={addCloseRef}></button>
                        </div>
                        <div className="modal-body">
                            <form id="addForm" className="row g-3" onSubmit={onAdd}>
                                <div className="col-md-6">
                                    <label className="form-label">Name</label>
                                    <input type="text" className="form-control" name="name" required value={exName} onChange={(e) => setExname(e.target.value)} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Email</label>
                                    <input type="email" className="form-control" name="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Contact</label>
                                    <input type="number" className="form-control" name="contact" required value={contact} onChange={(e) => setContact(e.target.value)} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Job title</label>
                                    <select className="form-select" name="jobTitle" value={exJobTitle} onChange={(e) => setExJobtitle(e.target.value)}>
                                        <option value="">Not set</option>
                                        {
                                            jobTitles.map((j, index) =>
                                                <option key={index} value={j}>{j}</option>
                                            )}
                                    </select>
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Branch</label>
                                    <select className="form-select" name="branchId" required value={branchId} onChange={(e) => setBranchId(e.target.value)}>
                                        <option value="">Select branch</option>
                                        {branches.map((b, index) => <option key={index} value={b.id}>{b.name} ({b.ifscCode})</option>)}
                                    </select>
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Username</label>
                                    <input type="text" className="form-control" name="username" required value={username} onChange={(e) => setUsername(e.target.value)} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Password</label>
                                    <input type="password" className="form-control" name="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
                                </div>
                            </form>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                            <button type="submit" form="addForm" className="btn btn-maverick">Add</button>
                        </div>
                    </div>
                </div>
            </div>

            <div className="modal fade" id="jobModal" tabIndex="-1" aria-hidden="true" data-bs-focus="false">
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">Job title of {editExec?.name}</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal" ref={jobCloseRef}></button>
                        </div>
                        <div className="modal-body">
                            <label className="form-label">Job title</label>
                            <select className="form-select" value={newJobTitle} onChange={(e) => setNewJobTitle(e.target.value)}>
                                <option value="">Select job title</option>
                                {jobTitles.map((j, index) => <option key={index} value={j}>{j}</option>)}
                            </select>

                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" className="btn btn-maverick" onClick={() => onSaveJobTitle()} >Save</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Executives
