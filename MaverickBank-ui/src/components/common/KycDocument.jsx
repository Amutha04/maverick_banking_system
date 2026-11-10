function KycDocument({ label, file }) {
    const url = `/customerUploads/${file}`
    const isImage = /\.(jpg|jpeg|png)$/i.test(file ?? "")

    return (
        <div className="col-6 col-md-3 text-center">
            <div className="small text-muted mb-1">{label}</div>
            {!file
                ? <span className="text-muted small">Not uploaded</span>
                : isImage
                    ? <a href={url} target="_blank" rel="noreferrer"><img src={url} alt={label} className="img-thumbnail" style={{ maxHeight: "110px" }} /></a>
                    : <a href={url} target="_blank" rel="noreferrer" className="btn btn-sm btn-outline-secondary">Open file</a>}
        </div>
    )
}

export default KycDocument
