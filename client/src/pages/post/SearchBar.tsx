import type { PostSearchParams } from "../../types/Post"

interface Props {
    searchParams: PostSearchParams,
    setSearchParams: (params: PostSearchParams) => void,
    onSearch: () => void
}

//props는 한 객체만 전달 가능 구조분해로 사용
export const SearchBar = ({ searchParams, setSearchParams, onSearch }: Props) => {
    return (
        <div>
            <select
                value={searchParams.type}
                onChange={(e) => setSearchParams({ ...searchParams, type: e.target.value })}>
                <option>제목</option>
                <option>내용</option>
                <option>제목+내용</option>
                <option>작성자</option>
            </select>
            <input type="text"
                value={searchParams.keyword}
                onChange={(e) => setSearchParams({ ...searchParams, keyword: e.target.value })} />
            <input type="date"
                value={searchParams.startDate ?? ""}
                onChange={(e) => setSearchParams({ ...searchParams, startDate: e.target.value })} />
            <span>~</span>
            <input type="date"
                value={searchParams.endDate ?? ""}
                onChange={(e) => setSearchParams({ ...searchParams, endDate: e.target.value })} />
            <button>1주일</button>
            <button>1개월</button>
            <button>3개월</button>
            <button onClick={onSearch}>조회</button>
        </div>
    )
}