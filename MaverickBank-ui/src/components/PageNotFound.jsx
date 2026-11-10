import { useEffect } from "react"

function PageNotFound() {

    useEffect(() => {
        localStorage.clear()
    },[])
    
    return(
        <div>
            <h1>Page not found</h1>
        </div>
        
    )
}

export default PageNotFound