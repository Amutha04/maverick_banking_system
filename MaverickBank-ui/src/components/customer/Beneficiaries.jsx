import { useDispatch, useSelector } from "react-redux"
import { useState } from "react"
import { API_SERVER_URL_DEV } from "../../config"
import axios from "axios"
import { getBeneficiaries } from "../../store/actions/CustomerActions"

function Beneficiaries() {
    const beneficiaryList = useSelector(state => state.beneficiary)
    const branchList = useSelector(state => state.branches)
    const [branch, setBranch] = useState("")
    const selectedBranch = branchList.find(br => br.name === branch)

    const dispatch = useDispatch()
    const [name, setName] = useState("")
    const [accountNumber, setAccountNumber] = useState("")
    

    const addBeneficiary = async(e) => {
        e.preventDefault()
        try{
            let obj = {
                name : name,
                accountNumber : accountNumber,
                branchName : branch,
                ifscCode : selectedBranch?.ifscCode
            }

            await axios.post(`${API_SERVER_URL_DEV}/api/beneficiary/add`, obj, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })

            window.Swal.fire({
                icon: "success",
                title: "Beneficiary Added",
                showConfirmButton: false,
                timer: 1500
            })
            dispatch(getBeneficiaries())
            
        }
        catch(err){

            window.Swal.fire({
                icon: "error",
                title: "Operation Failed",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
        }

    const onDelete = async(id) => {
        try{
            await axios.delete(`${API_SERVER_URL_DEV}/api/beneficiary/delete/${id}`, {
                headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
            })

            window.Swal.fire({
                icon: "success",
                title: "Beneficiary Deleted",
                showConfirmButton: false,
                timer: 1500
            })
            dispatch(getBeneficiaries())
        }
        catch(err){

            window.Swal.fire({
                icon: "error",
                title: "Operation Failed",
                text: err?.response?.data?.message || "Something went wrong",
                confirmButtonText: "OK"
            })
        }
    }
    
    return (
        <div className="container-fluid p-3 p-md-4">

            <div className="card mb-4"><div className="card-header bg-white fw-semibold">Add beneficiary</div><div className="card-body">
                <form className="row g-3 needs-validation" data-success="Beneficiary added." onSubmit={(e) => addBeneficiary(e)}>
                    <div className="col-md-6">
                        <label className="form-label">Account holder name</label>
                        <input type="text" className="form-control" placeholder="" onChange={(e)=>setName(e.target.value)} required /></div>
                    <div className="col-md-6"><label className="form-label">Account number</label><input type="text" className="form-control" onChange={(e)=>setAccountNumber(e.target.value)} required pattern="[0-9]{9,18}" /></div>
                    <div className="col-md-4"><label className="form-label">Bank</label><select id="bank" className="form-select" required>
                        <option value="">Select bank</option>
                        <option value="Maverick">Maverick</option>
                    </select></div>
                    <div className="col-md-4">
                        <label className="form-label">Branch</label>
                        <select id="branch" className="form-select" onChange={(e) => setBranch(e.target.value)} required>
                            <option value="">Select branch</option>
                            {
                                        branchList.map((br, index) => (
                                            <option key={index} value={br.name}>
                                                {br.name} - {br.district}
                                            </option>
                                        ))
                                    }
                        </select>
                    </div>
                    <div className="col-md-4"><label className="form-label">IFSC code</label><input id="ifsc" className="form-control" readOnly placeholder="Auto-filled" value={selectedBranch?.ifscCode ?? ""}/></div>
                    <div className="col-12"><button className="btn btn-maverick">Add beneficiary</button></div></form></div></div>
            <div className="card"><div className="card-header bg-white fw-semibold">Saved beneficiaries</div><div className="table-responsive"><table className="table table-hover align-middle mb-0">
                <thead className="table-light"><tr>
                    <th>S.No</th>
                    <th>Name</th>
                    <th>Account Number</th>
                    <th>Branch</th>
                    <th>IFSC</th>
                    <th>Added On</th>
                    <th></th>
                    </tr></thead>
                <tbody>
                    {
                        beneficiaryList.map((b,index) => (
                            <tr key={index}>
                                <td>{index+1}</td>
                                <td>{b.name}</td>
                                <td>{b.accountNumber}</td>
                                <td>{b.branchName}</td>
                                <td>{b.ifscCode}</td>
                                <td>{b.addedOn.split('T')[0]}</td>
                                <td><button className="btn btn-maverick" onClick={()=>onDelete(b.id)}>Delete</button></td>
                            </tr>
                        ))
                    }
                    
                </tbody></table></div></div>
        </div>
    )
}

export default Beneficiaries