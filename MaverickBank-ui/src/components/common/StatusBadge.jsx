function StatusBadge({ status }) {
    const colours = {
        ACTIVE: "success",
        APPROVED: "success",
        SUCCESS: "success",
        OPEN: "success",
        DONE: "success",
        VERIFIED: "success",
        IN_PROCESS: "warning",
        PENDING: "warning",
        CLOSURE_REQUEST: "warning",
        SANCTIONED: "info",
        ACCEPTED: "info",
        REJECTED: "danger",
        FAILED: "danger",
        CLOSED: "danger",
        SUSPENDED: "danger"
    }

    if (!status)
        return <span className="text-muted">-</span>

    return (
        <span className={`badge text-bg-${colours[status] ?? "secondary"}`}>
            {status}
        </span>
    )
}

export default StatusBadge
