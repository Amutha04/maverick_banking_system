import { configureStore } from "@reduxjs/toolkit";
import CustomerReducer from "./reducers/CustomerReducer";

export const store = configureStore({
    reducer : CustomerReducer
})