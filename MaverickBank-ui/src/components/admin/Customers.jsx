import axios from "axios"
import { useEffect, useRef, useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import StatusBadge from "../common/StatusBadge"
import Pagination from "../common/Pagination"
import KycDocument from "../common/KycDocument"

function Customers() {

    const [customers, setCustomers] = useState([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalRecords, setTotalRecords] = useState(0)
    const [recordsPerPage, setRecordsPerPage] = useState(0)
    const [page, setPage] = useState(0)
    const size = 7
    const [reload, setReload] = useState(0)

    const [nameInput, setNameInput] = useState("")
    const [name, setName] = useState("")
    const [gender, setGender] = useState("")
    const [kycStatus, setKycStatus] = useState("")
    const [createdFrom, setCreatedFrom] = useState("")
    const [createdTo, setCreatedTo] = useState("")

    const [customer, setCustomer] = useState(null)
    const [details, setDetails] = useState(null)
    const closeRef = useRef(null)

    useEffect(() => {
        const getCustomers = async () => {
            try {
                const params = { page, size }
                if (name) params.name = name
                if (gender) params.gender = gender
                if (kycStatus) params.kycStatus = kycStatus
                if (createdFrom) params.createdFrom = createdFrom
                if (createdTo) params.createdTo = createdTo

                const response = await axios.get(`${API_SERVER_URL_DEV}/api/customer/filter`, { params, 
                    headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
                 })

                setCustomers(response.data.list)
                setTotalPages(response.data.paginationInfo.totalPages)
                setTotalRecords(response.data.paginationInfo.totalRecords)
                setRecordsPerPage(response.data.paginationInfo.recordsPerPage)
            }
            catch (err) {
                window.Swal.fire({
                    icon: "error",
                    title: "Could not load customers",
                    text: err?.response?.data?.message || "Something went wrong",
                    confirmButtonText: "OK"
                })
            }
        }
        getCustomers()
    }, [page, size, name, gender, kycStatus, createdFrom, createdTo, reload])

    const onFilterChange = (setter) => (e) => {
        setter(e.target.value)
        setPage(0)
    }

    const onSearch = (e) => {
        e.preventDefault()
        setName(nameInput.trim())
        setPage(0)
    }

    const onClear = () => {
        setNameInput("")
        setName("")
        setGender("")
        setKycStatus("")
        setCreatedFrom("")
        setCreatedTo("")
        setPage(0)
    }


    const onView = async (c) => {
        setCustomer(c)
        setDetails(null)
        try {
            const d = await axios.get(`${API_SERVER_URL_DEV}/api/customer/${c.id}`, { 
                headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
             })
            setDetails(d.data)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not load the customer",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }


    const onDeactivate = async () => {
        const result = await window.Swal.fire({
            icon: "warning",
            title: "Deactivate the customer?",
            text: `${customer.name} cannot log in any more.`,
            showCancelButton: true,
            confirmButtonText: "Deactivate",
            confirmButtonColor: "#dc3545"
        })
        console.log(result)
        if (!result.isConfirmed) return

        try {
            await axios.put(`${API_SERVER_URL_DEV}/api/customer/delete/${customer.id}`, null, { 
                headers : { Authorization: 'Bearer ' + localStorage.getItem('token') }
             })
            closeRef.current.click()
            window.Swal.fire({
                icon: "success",
                title: "Customer deactivated",
                showConfirmButton: false,
                timer: 1500
            })
            setReload(reload + 1)
        }
        catch (err) {
            window.Swal.fire({
                icon: "error",
                title: "Could not deactivate",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }

    return (
        <div>
            <div className="card mb-4">
                <div className="card-body">
                    <form className="row g-3 align-items-end" onSubmit={onSearch}>
                        <div className="col-md-3">
                            <label className="form-label">Name</label>
                            <div className="input-group">
                                <input type="text" className="form-control" placeholder="Search" value={nameInput} onChange={(e) => setNameInput(e.target.value)} />
                                <button type="submit" className="btn btn-outline-secondary"><i className="bi bi-search"></i></button>
                            </div>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Gender</label>
                            <select className="form-select" value={gender} onChange={()=>onFilterChange(setGender)}>
                                <option value="">All</option>
                                <option value="MALE">MALE</option>
                                <option value="FEMALE">FEMALE</option>
                                <option value="OTHER">OTHER</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">KYC</label>
                            <select className="form-select" value={kycStatus} onChange={()=>onFilterChange(setKycStatus)}>
                                <option value="">All</option>
                                <option value="PENDING">PENDING</option>
                                <option value="DONE">DONE</option>
                            </select>
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Joined from</label>
                            <input type="date" className="form-control" value={createdFrom} onChange={()=>onFilterChange(setCreatedFrom)} />
                        </div>
                        <div className="col-md-2">
                            <label className="form-label">Joined to</label>
                            <input type="date" className="form-control" value={createdTo} onChange={()=>onFilterChange(setCreatedTo)} />
                        </div>
                        <div className="col-md-1 d-grid">
                            <button type="button" className="btn btn-outline-secondary" onClick={()=>onClear()}>Clear</button>
                        </div>
                    </form>
                </div>
            </div>

            <div className="card">
                <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                        <thead className="table-light">
                            <tr>
                                <th>S.No</th>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Gender</th>
                                <th>Date of birth</th>
                                <th>Contact</th>
                                <th>Email</th>
                                <th className="text-end">CIBIL</th>
                                <th>KYC</th>
                                <th>Joined on</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {customers.length === 0 && <tr><td colSpan="11" className="text-center text-muted py-4">No customers found</td></tr>}
                            {customers.map((c, index) => (
                                <tr key={index}>
                                    <td>{page * size + index + 1}</td>
                                    <td>#{c.id}</td>
                                    <td>{c.name}</td>
                                    <td>{c.gender ?? "-"}</td>
                                    <td>{c.dob}</td>
                                    <td>{c.contact}</td>
                                    <td>{c.email ?? "-"}</td>
                                    <td className="text-end">{c.cibilScore ?? "-"}</td>
                                    <td><StatusBadge status={c.kycStatus} /></td>
                                    <td>{c.createdAt}</td>
                                    <td className="text-end text-nowrap">
                                        <button type="button" className="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#custModal" onClick={() => onView(c)}>View</button>
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

            <div className="modal fade" id="custModal" tabIndex="-1" aria-hidden="true" data-bs-focus="false">
                <div className="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title fs-5">{customer ? `${customer.name} (#${customer.id})` : "Customer"}</h2>
                            <button type="button" className="btn-close" data-bs-dismiss="modal" ref={closeRef}></button>
                        </div>

                        <div className="modal-body">
                            {customer && (
                                <div className="row g-4">
                                    <div className="col-md-6">
                                        <h6 className="fw-semibold">Details</h6>
                                        {details &&
                                            <dl className="row mb-0 small">
                                                <dt className="col-5">Username</dt><dd className="col-7">{details.username}</dd>
                                                <dt className="col-5">Date of birth</dt><dd className="col-7">{details.dob}</dd>
                                                <dt className="col-5">Gender</dt><dd className="col-7">{details.gender ?? "-"}</dd>
                                                <dt className="col-5">Contact</dt><dd className="col-7">{details.contact}</dd>
                                                <dt className="col-5">Email</dt><dd className="col-7">{details.email ?? "-"}</dd>
                                                <dt className="col-5">Address</dt><dd className="col-7">{details.address ?? "-"}</dd>
                                                <dt className="col-5">Aadhaar</dt><dd className="col-7">{details.aadhaarNumber ?? "-"}</dd>
                                                <dt className="col-5">PAN</dt><dd className="col-7">{details.panNumber ?? "-"}</dd>
                                                <dt className="col-5">CIBIL score</dt><dd className="col-7">{customer.cibilScore ?? "-"}</dd>
                                                <dt className="col-5">KYC</dt><dd className="col-7"><StatusBadge status={details.kycStatus} /></dd>
                                                <dt className="col-5">Joined on</dt><dd className="col-7">{customer.createdAt}</dd>
                                            </dl>
                                        }
                                    </div>
                                    {details && (
                                        <div className="col-12">
                                            <h6 className="fw-semibold">KYC documents</h6>
                                            <div className="row g-3">
                                                <KycDocument label="Aadhaar" file={details.aadhaarUrl} />
                                                <KycDocument label="PAN" file={details.panUrl} />
                                                <KycDocument label="Photo" file={details.photoUrl} />
                                                <KycDocument label="Signature" file={details.signatureUrl} />
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        <div className="modal-footer">
                            {customer && <button type="button" className="btn btn-outline-danger me-auto" onClick={() => onDeactivate()}>Deactivate</button>}
                            <button type="button" className="btn btn-light" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Customers
