import { useState } from "react"
import { Link, useNavigate } from "react-router"
import { API_SERVER_URL_DEV } from "../../config"
import axios from "axios"

function Login() {
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [errMsg, setErrMsg] = useState("")
    const [errMsgComment, setErrMsgComment] = useState("")

    const navigate = useNavigate()

    const apiUrl = API_SERVER_URL_DEV + '/api/auth/login'
    const userDetailUrl = API_SERVER_URL_DEV + '/api/auth/user-details'

    const processLogin = async (e) => {
        e.preventDefault()
        const token = 'Basic ' + window.btoa(username + ":" + password)

        // console.log(token)

        try {
            const response = await axios.get(apiUrl, {
                headers: { Authorization: token }
            })

            // console.log(response)

            localStorage.setItem('token', response.data.token)
            localStorage.setItem('username', username)

            const resp = await axios.get(userDetailUrl, {
                headers : { Authorization : 'Bearer ' + response.data.token}
            })

            // console.log(resp)

            localStorage.setItem('role', resp.data.role)
            localStorage.setItem('jobTitle', resp?.data?.jobTitle)
            
            switch(resp.data.role) {
                case 'CUSTOMER' :
                    navigate(`customer/${response.data.token}`)
                    break;
                case 'ADMIN' :
                    navigate(`admin/${response.data.token}`)
                    break;
                case 'EXECUTIVE' :
                    if(resp?.data?.jobTitle === 'BANK_MANAGER')
                        navigate(`manager/${response.data.token}`)
                    else
                        navigate(`executive/${response.data.token}`)
                    break;
                default:
                    console.log("Something went wrong..")
                    break;
            }
        }
        catch (err) {
            if(err?.response?.data === undefined){
                setErrMsg("Invalid Credentials")
                // console.log(errMsg)
            }
            else{
                // console.log(err?.response?.data)
                setErrMsg(err?.response?.data?.message)
                setErrMsgComment(err?.response?.data?.comments)
                // console.log(errMsg)
                // console.log(errMsgComment)
                
            }
        }
    }


    return (
        <div className="container">
            <div className="row justify-content-center align-items-center min-vh-100">
                <div className="col-12 col-sm-9 col-md-7 col-lg-5 col-xl-4">

                    {/* <!-- Login Card --> */}
                    <div className="card shadow-sm border-0 rounded-4 p-4 p-sm-5 bg-white">
                        <div className="card-body p-0">

                            {/* <!-- Branding / Header --> */}
                            <div className="text-center mb-4">
                                <h1 className="h4 fw-bold text-maverick mb-1"><i className="bi bi-bank2 me-2"></i>Maverick Bank</h1>
                                <p className="text-muted small">Login your account to get started.</p>
                            </div>

                            {/* <!-- error / Header --> */}
                            <div className="text-center mb-4">

                                <p style={{ color: 'red', fontSize: '1.2rem' }}>
                                    {errMsg}
                                </p>
                                <p className="text-muted small">
                                    {errMsgComment}
                                </p>
                            </div>

                            {/* <!-- Login Form --> */}
                            <form onSubmit={(e) => processLogin(e)}>
                                {/* <!-- Username Field --> */}
                                <div className="mb-3">
                                    <label className="form-label small fw-semibold text-secondary">Username</label>
                                    <input type="text" className="form-control form-control-lg" required
                                        onChange={(e) => setUsername(e.target.value)} />
                                </div>

                                {/* <!-- Password Field --> */}
                                <div className="mb-3">
                                    <div className="d-flex justify-content-between align-items-center">
                                        <label className="form-label small fw-semibold text-secondary">Password</label>
                                    </div>
                                    <input type="password" className="form-control form-control-lg" placeholder="••••••••" required
                                        onChange={(e) => setPassword(e.target.value)} />
                                </div>

                                {/* <!-- Submit Button --> */}
                                <div className="d-grid mb-3">
                                    <button type="submit" className="btn btn-maverick btn-lg fw-semibold" onClick={() => {
                                        setErrMsg("")
                                        setErrMsgComment("")
                                    }} >Sign in</button>
                                </div>

                                {/* <!-- Footer Link --> */}
                                <div className="text-center">
                                    <p className="small text-muted mb-0">Don't have an account? <Link to="/customer/signup" className="text-maverick text-decoration-none fw-semibold">Sign up</Link></p>
                                </div>
                            </form>

                        </div>
                    </div>

                </div>
            </div>
        </div>
    )
}

export default Login