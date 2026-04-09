import { useState } from "react"
import type { LoginRequest } from "../../types/Auth";
import { login } from "../../api/apis/authApi";
import { useNavigate } from "react-router-dom";
import { RegisterPage } from "../user/RegisterPage";

export const LoginPage = () => {
    const navigate = useNavigate();
    const [form, setForm] = useState<LoginRequest>({
        email: "",
        password: ""
    });

    const handleLogin = () => {
        login(form)
            .then(() => navigate("/"))
            .catch((err) => {
                console.log(err.response.data)
                alert(err.response.data.message)
            })
    }

    return (
        <div>
            <h2>로그인</h2>
            <div>
                <form>
                    <label>email</label>
                    <input type="text"
                        onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
                    <label>password</label>
                    <input type="password"
                        onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} />
                </form>
                <button onClick={handleLogin}>Login</button>
                <button onClick={() => window.location.href = " http://localhost:8080/oauth2/authorization/google"}>google 로그인</button>
                <button onClick={() => navigate("/register")}>register</button>
            </div>
        </div>
    )
}