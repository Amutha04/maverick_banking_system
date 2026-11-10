
function NavBar({title, username}) {
    return (
        <header className="navbar bg-white border-bottom px-3 sticky-top">
            <div className="d-flex align-items-center">
                <button className="btn btn-outline-secondary d-md-none me-2" data-bs-toggle="offcanvas" data-bs-target="#sidebar" aria-label="Menu"><i className="bi bi-list"></i></button>
                <h1 className="h5 mb-0">{title}</h1>
            </div>
            <div className="dropdown">
                <button className="btn btn-light"><i className="bi bi-person-circle me-1"></i>{username}</button>
            </div>
        </header>
    )
}

export default NavBar