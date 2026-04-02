import { useState } from "react"
import type { RegisterRequest } from "../../types/User";
import { register } from "../../api/apis/userApi";
import { useNavigate } from "react-router-dom";

export const RegisterPage = () => {
    const navigate = useNavigate();
    const [form, setForm] = useState<RegisterRequest>({
        name: "",
        email: "",
        password: ""
    });

    const handleRegister = () => {
        register(form)
            .then(() => navigate("/"))
    }

    return (
        <div>
            <h2>회원가입</h2>
            <form>
                <div>
                    <label>name</label>
                    <input type="text"
                        onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))} />
                </div>
                <div>
                    <label>email</label>
                    <input type="text"
                        onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} />
                </div>
                <div>
                    <label>password</label>
                    <input type="text"
                        onChange={(e) => {
                            setForm((prev) => ({ ...prev, password: e.target.value }))
                        }}
                    />
                    {form.password && (
                        <div>
                            <p>{form.password.length >= 8 ? "완료" : "8자 이상"}</p>
                            <p>{/[A-Z]/.test(form.password) ? "완료" : "대문자 포함"}</p>
                            <p>{/[0-9]/.test(form.password) ? "완료" : "숫자 포함"}</p>
                            <p>{/[!@#$%^&*]/.test(form.password) ? "완료" : "특수문자 포함"}</p>
                        </div>
                    )}
                </div>
            </form>
            <button onClick={handleRegister}>register</button>
        </div>
    )
}