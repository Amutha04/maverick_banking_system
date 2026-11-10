const initialState = {
    customerAccounts: [],
    customerLoans: [],
    customerTransactions: [],
    customerAccountRequests : [],
    branches : [],
    beneficiary : [],
    loanProducts : []
}

const CustomerReducer = (state = initialState, action) => {
    switch(action.type) {
        case 'FETCH_ACCOUNTS_CUSTOMER':
            return {
                ...state,
                customerAccounts : action.payload
            }
        case 'FETCH_LOANS_CUSTOMER':
            return {
                ...state,
                customerLoans : action.payload
            }
        case 'FETCH_TRANSACTIONS_CUSTOMER':
            return {
                ...state,
                customerTransactions : action.payload
            }
        case 'FETCH_ACC_REQUESTS_CUSTOMER':
            return {
                ...state,
                customerAccountRequests : action.payload
            }
         case 'FETCH_BRANCHES':
             return {
                 ...state,
                 branches : action.payload
             }
         case 'FETCH_BENEFICIARIES':
            return {
                 ...state,
                 beneficiary : action.payload
             }
         case 'FETCH_LOAN_PRODUCTS':
            return {
                 ...state,
                 loanProducts : action.payload
             }
        default:
            return state
    }
}

export default CustomerReducer