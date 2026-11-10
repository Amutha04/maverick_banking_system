function Pagination({ page, totalPages, totalRecords, recordsPerPage, onPageChange }) {

    if (totalRecords === 0)
        return null

    return (
        <nav aria-label="Page navigation">
            <ul className="pagination pagination-sm justify-content-center mb-2">
                <li className={`page-item ${page === 0 ? "disabled" : ""}`}>
                    <button className="page-link" onClick={() => onPageChange(page - 1)} disabled={page === 0}>Previous</button>
                </li>
                {
                    Array.from({ length: totalPages }, (e, index) => (
                        <li key={index} className={`page-item ${index === page ? "active" : ""}`}>
                            <button className="page-link" onClick={() => onPageChange(index)}>{index + 1}</button>
                        </li>
                    ))
                }
                <li className={`page-item ${page >= totalPages - 1 ? "disabled" : ""}`}>
                    <button className="page-link" onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1}>Next</button>
                </li>
            </ul>
            <p className="text-center text-muted small mb-0">
                {`Page ${page + 1} of ${totalPages}`}<br />
                {`Showing ${recordsPerPage} records from total ${totalRecords} records`}
            </p>
        </nav>
    )
}

export default Pagination
