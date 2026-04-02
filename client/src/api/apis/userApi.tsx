import type { RegisterRequest } from "../../types/User"
import api from "../axiosInstance"

export const register = (data: RegisterRequest) => {
    return api.post("/api/register", data)
}