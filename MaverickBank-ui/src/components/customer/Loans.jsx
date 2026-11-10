
import LoanList from "./LoanList";
import LoanProduct from "./LoanProduct";

function Loans() {

    return (
                <div className="container-fluid p-3 p-md-4">

                    <LoanProduct/>
                    <LoanList/>
                    
                </div>
        
    
    )
}

export default Loans