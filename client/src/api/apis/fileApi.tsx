//파일다운로드
export const downloadFile = async (fileId: number) => {
    const response = await fetch(`http://localhost:8080/api/files/download/${fileId}`, {
        credentials: 'include'
    });
    const url = await response.text();
    window.open(url, '_blank');
}

//파일바로보기
export const viewFile = async (fileId: number) => {
    const response = await fetch(`http://localhost:8080/api/files/view/${fileId}`, {
        credentials: 'include'
    });
    const url = await response.text();
    window.open(url, '_blank');
}


/**
 * [흐름]
 * window.open(`/api/files/download/1`) 
 *      -> 스프링 서버 -> DB에서 fileId=1 조회 
 *      -> secure_url 꺼내기
 *      -> 302 FOUND + Location: "https://res.cloudinary.com/.../file.jpg?fl_attachment=true
 *      -> 브라우저가 Cloudinary URL로 자동 이동
 *      -> 파일 다운로드 시작
 * 
 * [WHAT] api -> 302found -> 리다이렉트 -> 200ok
 *      - api가 직접 응답안하고 다른 url에서 받아야 할 때
 *      
 */