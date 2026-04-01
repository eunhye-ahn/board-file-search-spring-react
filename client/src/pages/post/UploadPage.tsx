import { useState } from "react"
import type { PostCreateRequest } from "../../types/Post";
import { createPost } from "../../api/apis/postApi";
import { useNavigate } from "react-router-dom";

export const UploadPage = () => {
    const navigate = useNavigate();
    const [form, setForm] = useState<PostCreateRequest>({
        title: "",
        content: ""
    });

    const [files, setFiles] = useState<File[]>([]);

    const handleUpload = () => {
        createPost(form, files)
            .then((res) => {
                console.log(res.data)
                navigate(`/posts/${res.data.postId}`)
            })
            .finally(() => console.log(form))
    }

    return (
        <div>
            <div>
                <h2>게시글 생성</h2>
            </div>
            {/**편집창*/}
            <div>
                <table>
                    <tbody>
                        <tr>
                            <th>제목</th>
                            <td>
                                <input type="text"
                                    onChange={(e) => setForm(prev => ({ ...prev, title: e.target.value }))} />
                            </td>
                        </tr>
                        <tr>
                            <th>내용</th>
                            <td>
                                <textarea value={form.content}
                                    onChange={(e) => setForm(prev => ({ ...prev, content: e.target.value }))} />
                            </td>
                        </tr>
                        <tr>
                            <th>첨부파일</th>
                            <td>
                                <input type="file" multiple
                                    onChange={(e) => {
                                        if (e.target.files) setFiles(Array.from(e.target.files))
                                    }} />
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            {/*하단 */}
            <div>
                <button onClick={() => navigate(-1)}>취소</button>
                <button onClick={handleUpload}>저장</button>
            </div>
        </div>

    )
}