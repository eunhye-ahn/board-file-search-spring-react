import { useEffect, useState } from "react";
import type { commentRequest, commentResponse } from "../../types/Comment";
import { addComment, deleteComment, getCommentsByPost, updateComment } from "../../api/apis/comment";

export const CommentSection = ({ postId }: { postId: number }) => {
    const [page, setPage] = useState(1);
    const [loading, setLoading] = useState(true);
    const [comments, setComments] = useState<commentResponse[]>([]);
    const [comment, setComment] = useState<commentRequest>();
    const [updateCommentId, setUpdateCommentId] = useState<number | null>();

    useEffect(() => {
        fetchComments();
    }, [page, postId, comment]);

    const fetchComments = () => {
        getCommentsByPost(postId, page)
            .then(res => {
                setComments(res.data.content)
            })
            .finally(() => setLoading(false))
    }

    const handleSubmitComment = () => {
        if (!comment) return;
        if (updateCommentId) {
            updateComment(updateCommentId, comment)
                .then(() => {
                    setComment({ content: "" })
                    setUpdateCommentId(null);
                })
        } else {
            addComment(postId, comment)
                .then(() => setComment({ content: "" })) //초기화 흐름 보충 필요
        }
    };

    //updateComment = (commentId: number, data: commentRequest) => {
    const handleUpdateComment = ({ commentId, content }: commentResponse) => {
        setUpdateCommentId(commentId);
        setComment({ content: content });
    }

    const handleDeleteComment = ({ commentId }: commentResponse) => {
        deleteComment(commentId)
            .then(() => fetchComments())
    }

    if (!postId) return <div>잘못된 접근입니다</div>;
    if (loading) return <div>로딩중...</div>
    if (!comments) return <div>데이터가 없습니다</div>;

    return (
        <div>
            {/**댓글리스트 */}
            <div>
                {comments
                    .map(comment => (
                        <div key={comment.commentId}>
                            <p>{comment.content}</p>
                            <button onClick={() => { handleUpdateComment(comment) }}>수정</button>
                            <button onClick={() => { handleDeleteComment(comment) }}>삭제</button>
                        </div>
                    ))}
                <button onClick={() => setPage(prev => prev - 1)}>이전</button>
                <button onClick={() => setPage(prev => prev + 1)}>다음</button>
            </div>
            {/**댓글인풋 */}
            <div>
                <textarea value={comment?.content}
                    onChange={(e) => setComment({ content: e.target.value })} />
                <button onClick={handleSubmitComment}>전송</button>
            </div>
        </div>
    )
}