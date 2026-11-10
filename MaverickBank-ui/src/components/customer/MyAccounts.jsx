import { useState } from "react"
import AccountList from "./AccountList"
import AccountRequestList from "./AccountRequestList"

function MyAccounts() {

    const [reload, setReload] = useState(0)

    return (

        <main className="container-fluid p-3 p-md-4">

            <AccountList  onRequested={() => setReload(reload + 1)}/>

            <AccountRequestList  reload={reload}/>

        </main>
    )
}


export default MyAccounts
