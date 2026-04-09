//댓글조회(페이징)
export interface commentResponse {
    commentId: number,
    content: string,
    createdAt: string,
    userName: string
}

export interface commentRequest {
    content: string
}