import type { FileByPostsListResponse, FileDetailResponse } from "./File";

//작성
export interface PostCreateRequest {
    title: string,
    content: string
}

//수정
export interface PostUpdateRequest {
    title: string,
    content: string,
    deleteFileIds: Number[]
}

//상세조회
export interface PostDetailResponse {
    postId: number,
    title: string,
    userName: string,
    content: string,
    createdAt: string,
    files: FileDetailResponse[]
}

//목록
export interface PostListResponse {
    content: PostItem[], //게시글 목록
    totalPages: number,  //페이지버튼 1-totalPages 만들기
    first: boolean, //처음버튼
    last: boolean,  //마지막버튼
    number: number //현재페이지
}

export interface PostItem {
    userName: string,
    postId: number,
    title: string,
    createdAt: string,
    viewCount: number,
    files: FileByPostsListResponse[]
}