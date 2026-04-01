import type { commentRequest } from "../../types/Comment";
import api from "../axiosInstance"

//댓글작성
export const addComment = (postId: number, data: commentRequest) => {
    return api.post(`/api/posts/${postId}/comments`, data);
}

//댓글삭제
export const deleteComment = (commentId: number) => {
    return api.delete(`/api/comments/${commentId}`);
}
//댓글수정
export const updateComment = (commentId: number, data: commentRequest) => {
    return api.put(`/api/comments/${commentId}`, data);
}

//댓글조회
export const getCommentsByPost = (postId: number, page: number) => {
    return api.get(`/api/posts/${postId}/comments`, {
        params: { page }
    });
}