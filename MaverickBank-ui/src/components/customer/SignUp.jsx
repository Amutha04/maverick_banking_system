import { useState } from "react"
import { Link, useNavigate } from "react-router"
import axios from "axios"
import { API_SERVER_URL_DEV } from "../../config"

function SignUp() {
    const [name, setName] = useState("")
    const [email, setEmail] = useState("")
    const [gender, setGender] = useState("")
    const [dob, setDob] = useState("")
    const [contact, setContact] = useState("")
    const [address, setAddress] = useState("")
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [confirmPassword, setConfirmPassword] = useState("")
    const [errMsg, setErrMsg] = useState("")
    const [errMsgComment, setErrMsgComment] = useState("")

    const navigate = useNavigate()
    const signupUrl = API_SERVER_URL_DEV + '/api/customer/add' 

    const passwordsMatch = password === confirmPassword

    const processSignUp = async (e) => {
        e.preventDefault()
        setErrMsg("")
        setErrMsgComment("")

        if (!passwordsMatch) {
            setErrMsg("Passwords do not match.")
            return
        }

        let newObj = {
            name : name,
            gender : gender,
            dob : dob,
            address : address,
            email : email,
            contact : contact,
            username : username,
            password : password
        }

        try {
            await axios.post(signupUrl, newObj)
            navigate("/")  
        }
        catch (err) {
            if (err?.response?.data === undefined) {
                setErrMsg("Could not reach the server. Please try again.")
            } else {
                setErrMsg(err.response.data.message)
                setErrMsgComment(err.response.data.comments)
            }
        }
        
    }

    return (
        <div className="container">
            <div className="row justify-content-center align-items-center min-vh-100 py-4">
                <div className="col-12 col-sm-10 col-md-8 col-lg-6 col-xl-5">

                    <div className="card shadow-sm border-0 rounded-4 p-4 p-sm-5 bg-white">
                        <div className="card-body p-0">

                            <div className="text-center mb-4">
                                <h1 className="h4 fw-bold text-maverick mb-1"><i className="bi bi-bank2 me-2"></i>Maverick Bank</h1>
                                <p className="text-muted small">Create your account to get started.</p>
                            </div>

                            {errMsg && (
                                <div className="alert alert-danger py-2" role="alert">
                                    {errMsg}
                                    {errMsgComment && <div className="small">{errMsgComment}</div>}
                                </div>
                            )}

                            <form onSubmit={(e) => processSignUp(e)}>

                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Full name</label>
                                    <input type="text" className="form-control" required minLength={3}
                                        value={name} onChange={(e) => setName(e.target.value)} />
                                </div>

                                <div className="row">
                                    <div className="col-md-6 mb-3">
                                        <label className="form-label small fw-semibold text-secondary">Gender</label>
                                        <select className="form-select" required value={gender}
                                            onChange={(e) => setGender(e.target.value)}>
                                            <option value="">Select</option>
                                            <option value="MALE">Male</option>
                                            <option value="FEMALE">Female</option>
                                            <option value="OTHER">Other</option>
                                        </select>
                                    </div>
                                    <div className="col-md-6 mb-3">
                                        <label className="form-label small fw-semibold text-secondary">Contact number</label>
                                        <input type="tel" className="form-control" required
                                            maxLength={10} placeholder="10-digit mobile number"
                                            title="Enter a valid 10-digit mobile number"
                                            value={contact}
                                            onChange={(e) => setContact(e.target.value)} />
                                    </div>
                                </div>

                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Date of Birth</label>
                                    <input type="date" className="form-control" required
                                        value={dob} onChange={(e) => setDob(e.target.value)} />
                                </div>

                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Email</label>
                                    <input type="email" className="form-control" required
                                        value={email} onChange={(e) => setEmail(e.target.value)} />
                                </div>

                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Address</label>
                                    <textarea className="form-control" rows="2" required
                                        value={address} onChange={(e) => setAddress(e.target.value)} />
                                </div>

                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Username</label>
                                    <input type="text" className="form-control" required minLength={4}
                                        value={username} onChange={(e) => setUsername(e.target.value)} />
                                </div>

                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Password</label>
                                    <input type="password" className="form-control" required
                                        value={password} onChange={(e) => setPassword(e.target.value)} />
                                </div>

                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Confirm password</label>
                                    <input type="password"
                                        className={`form-control ${confirmPassword ? (passwordsMatch ? "is-valid" : "is-invalid") : ""}`}
                                        required value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)} />
                                    <div className="invalid-feedback">Passwords do not match.</div>
                                </div>

                                <div className="d-grid mb-3">
                                    <button type="submit" className="btn btn-maverick btn-lg fw-semibold">
                                        Sign Up
                                    </button>
                                </div>

                                <div className="text-center">
                                    <p className="small text-muted mb-0">Already have an account? <Link to="/" className="text-maverick text-decoration-none fw-semibold">Login</Link></p>
                                </div>
                            </form>

                        </div>
                    </div>

                </div>
            </div>
        </div>
    )
}

export default SignUp