import axios from "axios"
import { API_SERVER_URL_DEV } from "../../config"

export const getAccounts = ()=> async(dispatch)=> {
    const response = await axios.get(`${API_SERVER_URL_DEV}/api/account/get`,{
        headers : {Authorization : 'Bearer ' + localStorage.getItem('token')}
    })

    console.log(response.data)

    dispatch({
        type : 'FETCH_ACCOUNTS_CUSTOMER',
        payload : response.data
    })
}

export const getLoans = ()=> async(dispatch)=> {
    const response = await axios.get(`${API_SERVER_URL_DEV}/api/loan/get`,{
        headers : {Authorization : 'Bearer ' + localStorage.getItem('token')}
    })

    console.log(response.data)

    dispatch({
        type : 'FETCH_LOANS_CUSTOMER',
        payload : response.data
    })
}

export const getTransactions = ()=> async(dispatch)=> {
    const response = await axios.get(`${API_SERVER_URL_DEV}/api/transaction/get`,{
        headers : {Authorization : 'Bearer ' + localStorage.getItem('token')}
    })

    console.log(response.data)

    dispatch({
        type : 'FETCH_TRANSACTIONS_CUSTOMER',
        payload : response.data.list
    })
}

export const getAccountRequests = ()=> async(dispatch)=> {
    const response = await axios.get(`${API_SERVER_URL_DEV}/api/account-request/get`,{
        headers : {Authorization : 'Bearer ' + localStorage.getItem('token')}
    })

    console.log(response.data)

    dispatch({
        type : 'FETCH_ACC_REQUESTS_CUSTOMER',
        payload : response.data.list
    })
}

export const getBranch = ()=> async(dispatch)=> {
    const response = await axios.get(`${API_SERVER_URL_DEV}/api/branch/all`,{
        headers : {Authorization : 'Bearer ' + localStorage.getItem('token')}
    })

    console.log(response.data)

    dispatch({
        type : 'FETCH_BRANCHES',
        payload : response.data
    })
}

export const getBeneficiaries = ()=> async(dispatch)=> {
    const response = await axios.get(`${API_SERVER_URL_DEV}/api/beneficiary`,{
        headers : {Authorization : 'Bearer ' + localStorage.getItem('token')}
    })

    console.log(response.data)

    dispatch({
        type : 'FETCH_BENEFICIARIES',
        payload : response.data
    })
}

export const getLoanProducts = ()=> async(dispatch)=> {
    const response = await axios.get(`${API_SERVER_URL_DEV}/api/loan-product/get`,{
        headers : {Authorization : 'Bearer ' + localStorage.getItem('token')}
    })

    console.log(response.data)

    dispatch({
        type : 'FETCH_LOAN_PRODUCTS',
        payload : response.data
    })
}