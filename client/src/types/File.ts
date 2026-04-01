//파일정보 - 게시글 상세조회
export interface FileDetailResponse {
    fileId: number,
    fileName: string,
    filePath: string,
    fileSize: number,
    fileType: string
};

//파일정보 - 게시글 전체조회
export interface FileByPostsListResponse {
    fileId: number
};

// //파일 바로보기
// export interface FileViewResponse {
//     fileId: number,
//     url: string,
//     fileName: string,
//     resourceType: string
// };

// //파일 다운로드
// export interface FileDownloadResponse {
//     fileName: string,
//     url: string
// }