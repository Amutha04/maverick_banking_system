import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { Doughnut } from 'react-chartjs-2'

ChartJS.register(ArcElement, Tooltip, Legend)

const statusColours = {
    ACTIVE: '#198754', APPROVED: '#198754', SUCCESS: '#198754', OPEN: '#198754', DONE: '#198754', VERIFIED: '#198754',
    IN_PROCESS: '#ffc107', PENDING: '#ffc107', CLOSURE_REQUEST: '#ffc107',
    SANCTIONED: '#0dcaf0', ACCEPTED: '#0dcaf0',
    REJECTED: '#dc3545', FAILED: '#dc3545', CLOSED: '#dc3545', SUSPENDED: '#dc3545'
}

const palette = ['#1d4f91', '#198754', '#ffc107', '#0dcaf0', '#dc3545', '#6f42c1', '#fd7e14']

function StatusChart({ title, labels = [], values = [] }) {
    const total = values.reduce((sum, v) => sum + v, 0)

    const data = {
        labels: labels,
        datasets: [
            {
                label: title,
                data: values,
                backgroundColor: labels.map((l, index) => statusColours[l] ?? palette[index % palette.length]),
                borderWidth: 1
            }
        ]
    }

    return (
        <div className="card h-100">
            <div className="card-header bg-white fw-semibold text-center">{title}</div>
            <div className="card-body d-flex justify-content-center align-items-center">
                {total === 0 ? <span className="text-muted">No data yet</span> : <Doughnut data={data} />}
            </div>
        </div>
    )
}

export default StatusChart
