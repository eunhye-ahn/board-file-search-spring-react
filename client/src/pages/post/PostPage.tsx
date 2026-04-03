import { useEffect, useState } from "react"
import { deletePost, getPost } from "../../api/apis/postApi";
import { useNavigate, useParams } from "react-router-dom";
import type { PostDetailResponse } from "../../types/Post";
import { downloadFile, viewFile } from "../../api/apis/fileApi";
import { CommentSection } from "../comment/CommentSection";

export const PostPage = () => {
    const { postId } = useParams<{ postId: string }>();
    const [data, setData] = useState<PostDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const postIdNum = Number(postId);

    /**
     * useEffect 앞에서 얼리리턴 X
     * 
     * ??보충필요
     * useEffect 안에 api 결과값 넣지 않기 > 무한 루프발생
     * 
     * api는 undefined를 항상 염두에 둠
     * 막는방법
     * 파라미터 : if문처리
     * 데이터: finally 처리
     */
    useEffect(() => {
        if (!postId) return;
        getPost(postIdNum)
            .then(res => setData(res.data))
            .finally(() => setLoading(false));
    }, [postIdNum]);

    const handleDeletePost = (postIdNum: number) => {
        deletePost(postIdNum)
            .then(() => navigate("/"))
    }

    if (!postId) return <div>잘못된 접근입니다</div>;
    if (loading) return <div>로딩중...</div>
    if (!data) return <div>데이터가 없습니다</div>;

    return (
        <div>
            {/*상단헤더 작성자만 보이게?? */}
            <div>
                <div>
                    <button onClick={() => navigate(-1)}>목록으로</button>
                    <h2>게시글 상세</h2>
                </div>
                <div>
                    <button onClick={() => navigate(`/posts/${postIdNum}/update`)}>수정</button>
                    <button onClick={() => handleDeletePost(postIdNum)}>삭제</button>
                </div>
            </div>
            {/*상세테이블*/}
            <table>
                <caption>{data.title}</caption>
                <tbody>
                    <tr>
                        <th>작성자</th>
                        <td>{data.userName}</td>
                        <th>등록일</th>
                        <td>{data.createdAt}</td>
                    </tr>
                    <tr>
                        <td colSpan={4}>{data.content}</td>
                    </tr>
                    <tr>
                        <th>첨부파일</th>
                        <td colSpan={3}>{data.files.map(file => (
                            <div>
                                <span key={file.filePath} onClick={() => downloadFile(file.fileId)}>{file.fileName}</span>
                                <button onClick={() => viewFile(file.fileId)}>바로보기</button>
                            </div>
                        )
                        )}</td>
                    </tr>
                </tbody>
            </table>
            {/*댓글목록*/}
            <CommentSection postId={postIdNum} />
        </div>
    )
}