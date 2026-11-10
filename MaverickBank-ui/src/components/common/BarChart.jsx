import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from 'chart.js'
import { Bar } from 'react-chartjs-2'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend)

function BarChart({ title, label, labels = [], values = [] }) {
    const total = values.reduce((sum, v) => sum + v, 0)

    const data = {
        labels: labels,
        datasets: [
            {
                label: label,
                data: values,
                backgroundColor: '#1d4f91',
                borderRadius: 4
            }
        ]
    }

    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
    }

    return (
        <div className="card h-100">
            <div className="card-header bg-white fw-semibold text-center">{title}</div>
            <div className="card-body" style={{ height: "300px" }}>
                {total === 0
                    ? <div className="d-flex h-100 justify-content-center align-items-center text-muted">No data yet</div>
                    : <Bar data={data} options={options} />}
            </div>
        </div>
    )
}

export default BarChart
